package com.example.pdf_extratct.readpdf.controllers;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.readpdf.dto.PdfProcessingRequest;
import com.example.pdf_extratct.readpdf.service.facade.PdfProcessingFacadeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Importações do Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Processamento de PDF", description = "APIs para upload, processamento e extração de dados de PDFs.")
public class PdfController {

    private final PdfProcessingFacadeService facadeService;

    @PostMapping("/api/pdf-to-excel")
    @Operation(summary = "Processa PDF e gera Excel",
            description = "Recebe um PDF, extrai texto, processa com IA e retorna um arquivo Excel com os dados extraídos.")
    @ApiResponse(responseCode = "200", description = "Excel gerado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou erro no processamento",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    public ResponseEntity<?> pdfToExcel(
            @Parameter(description = "Cabeçalhos desejados para a extração, separados por vírgula (ex: Nome,Idade,Cidade)", required = true)
            @RequestParam("headers") String headers,
            @Parameter(description = "Nome original do arquivo PDF para referência no job", required = true)
            @RequestParam("fileName") String fileName,
            @Parameter(description = "Tamanho do arquivo PDF em bytes", required = true)
            @RequestParam("fileSize") Long fileSize,
            @AuthenticationPrincipal UserEntity user,
            HttpServletRequest request
    ) {
        log.info("--- DEBUG AUTHENTICATION ---");
        log.info("User is null? {}", (user == null));
        if (user != null) {
            log.info("Authenticated User Email: {}", user.getEmail());
        }
        log.info("----------------------------");

        try {
            PdfProcessingRequest processingRequest = PdfProcessingRequest.builder()
                    .headers(headers)
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .user(user)
                    .clientIp(getClientIp(request))
                    .build();

            byte[] excelBytes = facadeService.processPdfToExcel(processingRequest);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dados.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);

        } catch (Exception e) {
            log.error("Erro processando requisição PDF", e);
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

