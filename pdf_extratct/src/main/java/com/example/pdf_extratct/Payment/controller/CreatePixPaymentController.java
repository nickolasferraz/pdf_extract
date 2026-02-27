package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.PixPaymentRequestDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentResponseDTO;
import com.example.pdf_extratct.Payment.service.CreatePixPaymentService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class CreatePixPaymentController {

    @Autowired
    private CreatePixPaymentService createPixPaymentService;

    @PostMapping("/pix")
    public ResponseEntity<PixPaymentResponseDTO> createPayment(@RequestBody PixPaymentRequestDTO requestDTO) throws MPException, MPApiException {
        PixPaymentResponseDTO response = createPixPaymentService.createPayment(requestDTO);
        return ResponseEntity.ok(response);
    }
}
