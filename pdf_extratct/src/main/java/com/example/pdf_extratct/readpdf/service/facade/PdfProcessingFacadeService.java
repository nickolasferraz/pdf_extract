package com.example.pdf_extratct.readpdf.service.facade;

import com.example.pdf_extratct.common.logging.ProgressLogger;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobService;
import com.example.pdf_extratct.readpdf.dto.PdfProcessingRequest;
import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionContext;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionFactory;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionStrategy;
import com.example.pdf_extratct.readpdf.service.aiservices.AiResponseParserService;
import com.example.pdf_extratct.readpdf.service.excelservices.ExcelService;
import com.example.pdf_extratct.readpdf.service.pdfservices.PdfTextAggregationService;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Padrão FACADE:
 * O objetivo desta classe é "esconder" a complexidade de múltiplos serviços.
 * Em vez do Controller conhecer o AIService, ExcelService, PdfService, JobService...
 * O Controller conhece APENAS o Facade. O Facade "orquestra" a banda.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingFacadeService {

    private final AiExtractionFactory aiFactory;
    private final AiResponseParserService aiparserService;
    private final ExcelService excelService;
    private final StorageService storageService;
    private final PdfTextAggregationService pdfTextAggregationService;
    private final ProcessingJobService processingJobService;
    private final com.example.pdf_extratct.loginpage.jobs.JobService queryJobService;
    private final IpBlockService ipBlockService;
    
    // Mantemos o logger do progresso
    private final ProgressLogger progress = new ProgressLogger();

    public byte[] processPdfToExcel(PdfProcessingRequest request) {
        ProcessingJobEntity job = null;
        int fileCount = 1;

        try {
            log.info("Iniciando orquestração do processamento do PDF via Facade.");

            // 1. Validar uso anônimo
            if (request.isAnonymous()) {
                if (!ipBlockService.registerAnonymousUse(request.clientIp(), fileCount)) {
                    throw new RuntimeException("Limite de uso anônimo excedido para este IP.");
                }
            }

            // 2. Extrair texto
            progress.step(10, "Lendo PDFs");
            String fullText = pdfTextAggregationService.aggreateFullText();
            int totalPages = pdfTextAggregationService.countTotalPages();

            if (fullText.isEmpty()) {
                log.error("Nenhum texto extraído do PDF.");
            }

            Integer estimatedCredits = Math.max(1, totalPages);

            // 3. Criar Job (Delegamos para o padrão Job Creation Factory criado no JobService)
            progress.step(20, "Criando job banco de dados");
            job = queryJobService.createJob(
                    request.user(),
                    request.fileName(),
                    request.fileSize(),
                    estimatedCredits,
                    request.clientIp()
            );

            log.info("Job criado com sucesso: {}", job.getJobId());

            // 4. Iniciar Processamento
            progress.step(30, "Iniciando processamento");
            processingJobService.startProcessing(job.getJobId());

            // 5. Enviar para a Inteligência Artificial
            progress.step(40, "Enviando texto/mídia para a IA");
            ExtractionHeaders extractionHeaders = new ExtractionHeaders(List.of(request.headers().split(",")));
            
            // Aqui aplicamos Inversão de Dependência! A IA não sabe onde as imagens estão. O Maestro (Facade) busca e entrega!
            List<Resource> files = new ArrayList<>();
            if (fullText.trim().length() < 200) {
                log.info("Texto muito pequeno, pegando arquivos físicos do storageService para OCR.");
                storageService.loadAll().forEach(path -> {
                    Resource resource = storageService.loadAsResource(path.getFileName().toString());
                    files.add(resource);
                });
            }

            AiExtractionContext aiContext = AiExtractionContext.builder()
                    .fullText(fullText)
                    .files(files)
                    .build();

            // Padrão Strategy + Factory para decidir se é OCR ou Scanner puramente de Texto!
            AiExtractionStrategy strategy = aiFactory.getStrategy(fullText.length());
            String excelJson = strategy.extract(extractionHeaders, aiContext);

            if (excelJson.isEmpty()) {
                log.error("Nenhuma resposta da IA.");
            }

            // 6. Fazer o parse (entender o JSON da IA e virar Map)
            progress.step(70, "Fazendo parse da resposta da IA");
            List<Map<String, String>> rows = aiparserService.parseRows(excelJson);

            // 7. Gerar Excel final em bytes
            progress.step(90, "Gerando arquivo Excel");
            byte[] excelBytes = excelService.generateExcel(rows);

            // 8. Finalizar o Job (Aqui ele já cobra os créditos na nossa strategy)
            progress.step(95, "Completando o Job no banco");
            if (request.isAnonymous()) {
                processingJobService.completeAnonymousJob(job.getJobId(), estimatedCredits, estimatedCredits);
            } else {
                processingJobService.completeJob(job.getJobId(), estimatedCredits, estimatedCredits);
            }

            // 9. Limpar sistema de arquivos temporário
            storageService.deleteAll();
            progress.finish("PDF para Excel finalizado com sucesso.");

            return excelBytes;

        } catch (Exception e) {
            handleFailure(request, job, fileCount, e);
            throw new RuntimeException("Falha no orquestrador: " + e.getMessage(), e);
        }
    }

    private void handleFailure(PdfProcessingRequest request, ProcessingJobEntity job, int fileCount, Exception e) {
        log.error("Erro durante o fluxo PDF->Excel", e);
        
        if (job != null) {
            processingJobService.failJob(job.getJobId(), e.getMessage());
        }

        if (request.isAnonymous()) {
            try {
                ipBlockService.refundAnonymousUse(request.clientIp(), fileCount);
            } catch (Exception ex) {
                log.error("Erro ao reembolsar uso anônimo para IP {}", request.clientIp(), ex);
            }
        }
    }
}
