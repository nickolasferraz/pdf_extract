package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.CardPaymentDTO;
import com.example.pdf_extratct.Payment.dto.PaymentCreateResponsetDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentRequestDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentResponseDTO;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final MercadoPagoClient mercadoPagoClient;
    // private final PaymentRepository paymentRepository; // Você injetaria seu repositório aqui

    @Override
    public PaymentCreateResponsetDTO processPayment(CardPaymentDTO cardPaymentDTO, String userId, Integer packageId) throws MPException, MPApiException {
        log.info("Processing payment for payer: {} with local userId: {} and packageId: {}", cardPaymentDTO.getPayer().getEmail(), userId, packageId);
        // Agora, extraímos tanto o pacote do frontend (DTO) quanto o usuário do Token (Controller)
        return mercadoPagoClient.processpayment(cardPaymentDTO, userId, packageId);
    }

    @Override
    public PixPaymentResponseDTO createPixPayment(PixPaymentRequestDTO request, String userId, Integer packageId) throws MPException, MPApiException {
        log.info("Processing Pix payment for payer: {} with local userId: {} and packageId: {}", request.payer().email(), userId, packageId);
        return mercadoPagoClient.createPixPayment(request, userId, packageId);
    }
}
