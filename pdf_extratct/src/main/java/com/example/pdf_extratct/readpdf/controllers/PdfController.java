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
import com.example.pdf_extratct.uploadfiles.storage.service.FileSystemStorageService;
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


@RestController
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);
    private final AIService aiService;
    private final AiResponseParserService aiparserService;
    private final ExcelService excelService;
    private final FileSystemStorageService fileSystemStorageService;
    private  final PdfTextAggregationService pdfTextAggregationService;
    private final ProgressLogger progress = new ProgressLogger();
    private final ProcessingJobService jobService;

    public PdfController(AIService aiService,
                         ExcelService excelService,
                         FileSystemStorageService fileSystemStorageService,
                         PdfTextAggregationService pdfTextAggregationService,
                         AiResponseParserService  aiparserService,
                         ProcessingJobService jobService
    ) {

        this.pdfTextAggregationService = pdfTextAggregationService;
        this.aiService = aiService;
        this.excelService= excelService;
        this.aiparserService=aiparserService;
        this.fileSystemStorageService = fileSystemStorageService;
        this.jobService = jobService;
    }

    private Integer estimateCredits(String fullText) {
        int estimatedPages = fullText.length() / 3000; // ~3000 chars por página
        return Math.max(1, estimatedPages); // Mínimo 1 crédito
    }

    @PostMapping("/api/pdf-to-excel")
    public ResponseEntity<?> pdfToExcel(
            @RequestParam("headers") String headers,
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") Long fileSize,
            @AuthenticationPrincipal UserEntity user  // 👈 NOVO - nome do arquivo

    ) {
        ProcessingJobEntity job = null;
        try {
            log.info("POST /api/pdf-to-excel started");

            ExtractionHeaders extractionHeaders =
                    new ExtractionHeaders(List.of(headers.split(",")));

            progress.step(10, "Reading PDFs");
            String fullText = pdfTextAggregationService.aggreateFullText();
            log.info("PDF parsed successfully. Text length: {}", fullText.length());
            Integer estimatedCredits = estimateCredits(fullText);
            log.info("Estimated credits: {}", estimatedCredits);

            // ===== ETAPA 2: CRIAR JOB (valida créditos automaticamente) =====
            progress.step(20, "Creating job");
            job = jobService.createJob(user, fileName, fileSize, estimatedCredits);
            // 👆 Aqui o jobService.createJob() JÁ VALIDA se o usuário tem créditos!
            // Se não tiver, lança RuntimeException("Créditos insuficiente")

            log.info("Job created: {}", job.getJobId());

            // ===== ETAPA 3: INICIAR PROCESSAMENTO =====
            progress.step(30, "Starting processing");
            jobService.startProcessing(job.getJobId());
            // 👆 Muda status do job de PENDING → PROCESSING



            progress.step(40, "Sending text to AI");
            String excelJson= aiService.exctract(extractionHeaders,fullText);

            progress.step(70, "Parsing AI response");
            List<Map<String, String>> rows = aiparserService.parseRows(excelJson);

            progress.step(90, "Generating Excel");
            byte[] excelBytes = excelService.generateExcel(rows);

            progress.step(95, "Completing job");
            int pagesProcessed = estimatedCredits; // Se você tem o número real de páginas, use aqui
            jobService.completeJob(job.getJobId(), pagesProcessed, estimatedCredits);
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
                // 👆 Marca job como FAILED, NÃO debita créditos
            }
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

}
