package com.example.pdf_extratct.loginpage.controllers;

import com.example.pdf_extratct.loginpage.credittransaction.CreditService;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.AddCreditsRequest;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.CreditBalanceResponse;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.TransactionResponse;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Importações do Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// REMOVA: import io.swagger.v3.oas.annotations.parameters.RequestBody;  ❌
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
@Tag(name = "Gerenciamento de Créditos", description = "APIs para consultar saldo e histórico de créditos, e adicionar créditos.")
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/balance")
    @Operation(summary = "Obter saldo de créditos", description = "Retorna o saldo de créditos atual do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Saldo de créditos retornado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditBalanceResponse.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    public ResponseEntity<CreditBalanceResponse> getBalance(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(creditService.getBalance(user));
    }

    @PostMapping("/add")
    @Operation(summary = "Adicionar créditos", description = "Adiciona uma quantidade específica de créditos ao saldo do usuário.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(  // ← Use nome completo para Swagger
            description = "Requisição para adicionar créditos",
            required = true,
            content = @Content(schema = @Schema(implementation = AddCreditsRequest.class)))
    @ApiResponse(responseCode = "200", description = "Créditos adicionados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditBalanceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida (ex: quantidade negativa)",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    public ResponseEntity<CreditBalanceResponse> addCredits(
            @AuthenticationPrincipal UserEntity user,
            @Valid @RequestBody AddCreditsRequest request) {  // ← Este é do Spring
        return ResponseEntity.ok(creditService.addCredits(user, request));
    }

    @GetMapping("/history")
    @Operation(summary = "Obter histórico de transações de crédito", description = "Retorna o histórico paginado de todas as transações de crédito do usuário.")
    @ApiResponse(responseCode = "200", description = "Histórico de transações retornado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @AuthenticationPrincipal UserEntity user,
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(creditService.getTransactionHistory(user, pageable));
    }
}
