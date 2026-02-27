package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.dto.CardPaymentDTO;
import com.example.pdf_extratct.Payment.dto.PaymentCreateResponsetDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentRequestDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentResponseDTO;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

public interface PaymentService {
    PaymentCreateResponsetDTO processPayment(CardPaymentDTO cardPaymentDTO) throws MPException, MPApiException;
    PixPaymentResponseDTO createPixPayment(PixPaymentRequestDTO request) throws MPException, MPApiException;
}
