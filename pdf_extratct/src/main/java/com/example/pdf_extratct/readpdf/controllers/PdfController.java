package com.example.pdf_extratct.readpdf.controllers;

import com.example.pdf_extratct.common.logging.ProgressLogger;
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

    public PdfController(AIService aiService,
                         ExcelService excelService,
                         FileSystemStorageService fileSystemStorageService,
                         PdfTextAggregationService pdfTextAggregationService,
                         AiResponseParserService  aiparserService) {

        this.pdfTextAggregationService = pdfTextAggregationService;
        this.aiService = aiService;
        this.excelService= excelService;
        this.aiparserService=aiparserService;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    @PostMapping("/api/pdf-to-excel")
    public ResponseEntity<?> pdfToExcel(@RequestParam("headers") String headers) {
        try {
            log.info("POST /api/pdf-to-excel started");

            ExtractionHeaders extractionHeaders =
                    new ExtractionHeaders(List.of(headers.split(",")));

            progress.step(10, "Reading PDFs");
            String fullText = pdfTextAggregationService.aggreateFullText();
            log.info("PDF parsed successfully. Text length: {}", fullText.length());

            progress.step(40, "Sending text to AI");
            String excelJson= aiService.exctract(extractionHeaders,fullText);

            progress.step(70, "Parsing AI response");
            List<Map<String, String>> rows = aiparserService.parseRows(excelJson);

            progress.step(90, "Generating Excel");
            byte[] excelBytes = excelService.generateExcel(rows);

            fileSystemStorageService.deleteAll();

            log.info("POST /api/pdf-to-excel finished successfully");
            progress.finish("PDF to Excel completed");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=dados.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);


        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

}
