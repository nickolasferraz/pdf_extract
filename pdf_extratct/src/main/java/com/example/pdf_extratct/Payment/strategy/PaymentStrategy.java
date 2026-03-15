package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;

public interface PaymentStrategy {

    // Identifica qual tipo de pagamento essa strategy atende
    PaymentType getType();

    // Lógica pura do pagamento
    // O try/catch e o tratamento do MPException agora são feitos pelo MercadoPagoWrapperImpl
    // que pode lançar PaymentGatewayException (RuntimeException).
    PaymentResult execute(PaymentRequest request, String userId, Integer packageId);
}
