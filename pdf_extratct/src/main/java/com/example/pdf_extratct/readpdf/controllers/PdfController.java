package com.example.pdf_extratct.readpdf.controllers;

import com.example.pdf_extratct.common.logging.ProgressLogger;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobService;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;
import com.example.pdf_extratct.readpdf.service.aiservices.AIService;
import com.example.pdf_extratct.readpdf.service.aiservices.AiResponseParserService;
import com.example.pdf_extratct.readpdf.service.excelservices.ExcelService;
import com.example.pdf_extratct.readpdf.service.pdfservices.PdfTextAggregationService;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.uploadfiles.storage.service.FileSystemStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// Importações do Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "Processamento de PDF", description = "APIs para upload, processamento e extração de dados de PDFs.") // Anotação da classe
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);
    private final AIService aiService;
    private final AiResponseParserService aiparserService;
    private final ExcelService excelService;
    private final FileSystemStorageService fileSystemStorageService;
    private  final PdfTextAggregationService pdfTextAggregationService;
    private final ProgressLogger progress = new ProgressLogger();
    private final ProcessingJobService jobService;
    private final IpBlockService ipBlockService;


    public PdfController(AIService aiService,
                         ExcelService excelService,
                         FileSystemStorageService fileSystemStorageService,
                         PdfTextAggregationService pdfTextAggregationService,
                         AiResponseParserService  aiparserService,
                         ProcessingJobService jobService,
                         IpBlockService ipBlockService
    ) {

        this.pdfTextAggregationService = pdfTextAggregationService;
        this.aiService = aiService;
        this.excelService= excelService;
        this.aiparserService=aiparserService;
        this.fileSystemStorageService = fileSystemStorageService;
        this.jobService = jobService;
        this.ipBlockService=ipBlockService;
    }

    private Integer estimateCredits(int totalPages) {
        return Math.max(1, totalPages);
    }


    @PostMapping("/api/pdf-to-excel")
    @Operation(summary = "Processa PDF e gera Excel",
            description = "Recebe um PDF, extrai texto, processa com IA e retorna um arquivo Excel com os dados extraídos.")
    @ApiResponse(responseCode = "200", description = "Excel gerado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary"))) // Resposta de sucesso
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou erro no processamento",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class))) // Resposta de erro
    @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content) // Exemplo para segurança
    @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content) // Exemplo para segurança
    public ResponseEntity<?> pdfToExcel(
            @Parameter(description = "Cabeçalhos desejados para a extração, separados por vírgula (ex: Nome,Idade,Cidade)", required = true)
            @RequestParam("headers") String headers,
            @Parameter(description = "Nome original do arquivo PDF para referência no job", required = true)
            @RequestParam("fileName") String fileName,
            @Parameter(description = "Tamanho do arquivo PDF em bytes", required = true)
            @RequestParam("fileSize") Long fileSize,
            @AuthenticationPrincipal UserEntity user ,// Não documentamos como um @Parameter direto para o usuário final
            HttpServletRequest request
    ) {
        ProcessingJobEntity job = null;
        String clientIp = getClientIp(request);
        boolean isAnonymous = (user == null);
        String jobId = null;
        int fileCount = 1; // Para este endpoint, sempre processamos 1 arquivo PDF por vez

        log.info("--- DEBUG AUTHENTICATION ---");
        log.info("User is null? {}", (user == null));
        log.info("isAnonymous: {}", isAnonymous);
        if (user != null) {
            log.info("Authenticated User ID: {}", user.getUserId());
            log.info("Authenticated User Email: {}", user.getEmail());
        }
        log.info("----------------------------");

        try {
            log.info("POST /api/pdf-to-excel started");

            // ===== ETAPA 1.5: VERIFICAR LIMITE ANÔNIMO (se aplicável) =====
            if (isAnonymous) {
                if (!ipBlockService.registerAnonymousUse(clientIp, fileCount)) {
                    throw new RuntimeException("Limite de uso anônimo excedido para este IP.");
                }
            }

            ExtractionHeaders extractionHeaders =
                    new ExtractionHeaders(List.of(headers.split(",")));


            progress.step(10, "Reading PDFs");
            String fullText = pdfTextAggregationService.aggreateFullText();
            int totalPages = pdfTextAggregationService.countTotalPages();

            if (fullText.isEmpty()) {
                log.error("PDF No parsed retun null text");
            }


            log.info("PDF parsed successfully. Text length: {}", fullText.length());
            Integer estimatedCredits = estimateCredits(totalPages);
            log.info("Estimated credits: {}", estimatedCredits);


            // ===== ETAPA 2: CRIAR JOB (valida créditos automaticamente) =====
            progress.step(20, "Creating job");

            if (isAnonymous) {
                // cria job anônimo e usa a mesma variável `job` para evitar confusão
                job = jobService.createAnonymousJob(fileName, fileSize, estimatedCredits, clientIp);

                if (job == null) {
                    throw new IllegalStateException("createAnonymousJob retornou null");
                }
                jobId = job.getJobId();
                log.info("Anonymous job created: {}", jobId);
            }else {
                job = jobService.createJob(user, fileName, fileSize, estimatedCredits);
            }
            log.info("Job created: {}", job.getJobId());

            // ===== ETAPA 3: INICIAR PROCESSAMENTO =====
            progress.step(30, "Starting processing");
            jobService.startProcessing(job.getJobId());
            // 👆 Muda status do job de PENDING → PROCESSING

            log.info("Job change or status for Processing", job.getJobId());


            progress.step(40, "Sending text to AI");
            String excelJson = aiService.exctract(extractionHeaders, fullText);

            if (excelJson.isEmpty()) {
                log.error("AI no extract text ");
            }

            log.info("AI response received");

            progress.step(70, "Parsing AI response");
            List<Map<String, String>> rows = aiparserService.parseRows(excelJson);

            if (rows.isEmpty()) {
                log.error("Ai no passing for rows");
            }

            log.info("AI response tasforme text em rows ");

            progress.step(90, "Generating Excel");
            byte[] excelBytes = excelService.generateExcel(rows);

            progress.step(95, "Completing job");
            int pagesProcessed = estimatedCredits;

            // CORREÇÃO: Chamar o método de conclusão apropriado APENAS UMA VEZ
            if (isAnonymous) {
                jobService.completeAnonymousJob(job.getJobId(), pagesProcessed, estimatedCredits);
            } else {
                jobService.completeJob(job.getJobId(), pagesProcessed, estimatedCredits);
            }
            // 👆 Aqui acontece a MÁGICA:
            //    1. Atualiza job para COMPLETED
            //    2. Salva quantas páginas foram processadas
            //    3. DEBITA automaticamente os créditos do usuário
            //    4. Cria registro em credit_transactions

            fileSystemStorageService.deleteAll();

            log.info("POST /api/pdf-to-excel finished successfully");
            progress.finish("PDF to Excel completed");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=dados.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);


        } catch (Exception e) {

            if (job != null) {
                jobService.failJob(job.getJobId(), e.getMessage());
            }

            if (isAnonymous) {
                try {
                    // CORREÇÃO: Passar fileCount para refundAnonymousUse
                    ipBlockService.refundAnonymousUse(clientIp, fileCount);
                } catch (Exception ex) {
                    log.error("Erro ao reembolsar uso anônimo para IP {}", clientIp, ex);
                }
            }

            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
