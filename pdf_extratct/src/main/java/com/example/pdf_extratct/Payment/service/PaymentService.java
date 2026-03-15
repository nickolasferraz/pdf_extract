package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;

public interface PaymentService {

    // Único método para qualquer tipo de pagamento — o type dentro do request define a strategy
    PaymentResult processPayment(PaymentRequest request, String userId, Integer packageId);
}
