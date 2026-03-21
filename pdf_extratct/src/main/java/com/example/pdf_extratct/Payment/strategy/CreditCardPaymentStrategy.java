package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.config.MPRequestOptionsFactory;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Value("${api.v1.mercadopago-notification-url}")
    private String notificationUrl;

    private final MercadoPagoWrapper mercadoPagoWrapper;

    @Override
    public PaymentType getType() {
        return PaymentType.CREDIT_CARD;
    }

    @Override
    public PaymentResult execute(PaymentRequest request, String userId, Integer packageId) {

        log.info("Processando CREDIT_CARD para payer: {} | userId: {} | packageId: {}",
                request.payer().email(), userId, packageId);

        // Garante que cardDetails foi enviado
        PaymentRequest.CardDetails card = request.cardDetails();
        if (card == null) {
            throw new IllegalArgumentException("cardDetails é obrigatório para CREDIT_CARD");
        }

        // Sanitiza o documento
        String identificationNumber = request.payer().identification().number();
        if (identificationNumber != null) {
            identificationNumber = identificationNumber.replaceAll("[^0-9]", "");
        }

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(request.transactionAmount())
                .token(card.token())                        // token gerado pelo SDK do MP no frontend
                .description(request.description())
                .installments(card.installments())          // número de parcelas
                .paymentMethodId(card.paymentMethodId())    // ex: "visa", "master"
                .notificationUrl(notificationUrl)
                .payer(
                        PaymentPayerRequest.builder()
                                .email(request.payer().email())
                                .firstName(request.payer().firstName())
                                .identification(
                                        IdentificationRequest.builder()
                                                .type(request.payer().identification().type())
                                                .number(identificationNumber)
                                                .build())
                                .build()
                )
                .externalReference(userId + "|" + packageId)
                .build();

        MPRequestOptions options = MPRequestOptionsFactory.createWithIdempotencyKey();
        Payment payment = mercadoPagoWrapper.createPayment(paymentCreateRequest, options);

        return new PaymentResult(
                String.valueOf(payment.getId()),
                payment.getStatus(),
                payment.getStatusDetail(),
                null,
                null,
                null,
                null
        );
    }
}
