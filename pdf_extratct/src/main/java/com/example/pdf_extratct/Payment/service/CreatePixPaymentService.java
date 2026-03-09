package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.PixPaymentRequestDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentResponseDTO;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePixPaymentService {

    private final MercadoPagoClient mercadoPagoClient;

    public PixPaymentResponseDTO createPayment(PixPaymentRequestDTO request, String userId) throws MPException, MPApiException {
        // O DTO já vem preenchido do Controller, então apenas delegamos para o Client
        return mercadoPagoClient.createPixPayment(request, userId, request.packageId());
    }
}
