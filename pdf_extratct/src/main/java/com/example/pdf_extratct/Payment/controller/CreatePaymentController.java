package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.CardPaymentDTO;
import com.example.pdf_extratct.Payment.dto.PaymentCreateResponsetDTO;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.Payment.service.PaymentService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class CreatePaymentController {

    private final PaymentService paymentService;

    @PostMapping("/CreditCard")
    public ResponseEntity<?> processPayment(
            @RequestBody CardPaymentDTO cardPaymentDTO,
            @AuthenticationPrincipal UserEntity user) {
        try {
            if (user == null || user.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }
            // Passa o ID do usuário local autenticado e o packageId
            PaymentCreateResponsetDTO response = paymentService.processPayment(cardPaymentDTO, user.getUserId().toString(), cardPaymentDTO.getPackageId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (MPApiException e) {
            // Erro vindo da API do Mercado Pago (ex: dados inválidos)
            return ResponseEntity.status(e.getStatusCode()).body(e.getApiResponse().getContent());
        } catch (MPException e) {
            // Erro na biblioteca do Mercado Pago (ex: configuração)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (RuntimeException e) {
            // Outros erros inesperados
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
