package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.Payment.service.PaymentService;
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

    // Endpoint único para PIX, Cartão e Checkout Pro
    // O paymentType dentro do body define qual strategy será usada
    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserEntity user) {
        try {
            if (user == null || user.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }

            PaymentResult result = paymentService.processPayment(
                    request,
                    user.getUserId().toString(),
                    request.packageId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

