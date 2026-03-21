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
public class PixPaymentStrategy implements PaymentStrategy {

    @Value("${api.v1.mercadopago-notification-url}")
    private String notificationUrl;

    private final MercadoPagoWrapper mercadoPagoWrapper;

    @Override
    public PaymentType getType() {
        return PaymentType.PIX;
    }

    @Override
    public PaymentResult execute(PaymentRequest request, String userid, Integer packageId) {

        log.info("Processando PIX para payer: {} | userId: {} | packageId: {}",
                request.payer().email(), userid, packageId);

        String identificationNumber = request.payer().identification().number();
        if (identificationNumber != null) {
            identificationNumber = identificationNumber.replaceAll("[^0-9]", "");
        }

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(request.transactionAmount())
                .description(request.description())
                .paymentMethodId("pix")                     // fixo para PIX
                .notificationUrl(notificationUrl)
                .payer(
                        PaymentPayerRequest.builder()
                                .email(request.payer().email())
                                .firstName(request.payer().firstName())
                                .lastName(request.payer().lastName())
                                .identification(
                                        IdentificationRequest.builder()
                                                .type(request.payer().identification().type())
                                                .number(identificationNumber)
                                                .build())
                                .build()
                )
                .externalReference(userid + "|" + packageId)
                .build();

        MPRequestOptions options = MPRequestOptionsFactory.createWithIdempotencyKey();
        Payment payment = mercadoPagoWrapper.createPayment(paymentCreateRequest, options);

        return mapToPaymentResult(payment);
    }

    private PaymentResult mapToPaymentResult(Payment payment) {
        var poi = payment.getPointOfInteraction();
        var transactionData = poi != null ? poi.getTransactionData() : null;

        return new PaymentResult(
                String.valueOf(payment.getId()),
                payment.getStatus(),
                payment.getStatusDetail(),
                transactionData != null ? transactionData.getQrCode() : null,
                transactionData != null ? transactionData.getQrCodeBase64() : null,
                transactionData != null ? transactionData.getTicketUrl() : null,
                null
        );
    }
}
