package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckoutProPaymentStrategy implements PaymentStrategy {

    @Value("${api.v1.mercadopago-notification-url}")
    private String notificationUrl;

    private final MercadoPagoWrapper mercadoPagoWrapper;

    @Override
    public PaymentType getType() {
        return PaymentType.CHECKOUT_PRO;
    }

    @Override
    public PaymentResult execute(PaymentRequest request, String userId, Integer packageId) {

        log.info("Processando CHECKOUT_PRO para payer: {} | userId: {} | packageId: {}",
                request.payer().email(), userId, packageId);

        // Garante que checkoutProDetails foi enviado
        PaymentRequest.CheckoutProDetails checkout = request.checkoutProDetails();
        if (checkout == null || checkout.items() == null || checkout.items().isEmpty()) {
            throw new IllegalArgumentException("checkoutProDetails com items é obrigatório para CHECKOUT_PRO");
        }

        // Monta a lista de itens da preference
        List<PreferenceItemRequest> items = checkout.items().stream()
                .map(item -> PreferenceItemRequest.builder()
                        .id(item.id())
                        .title(item.title())
                        .quantity(item.quantity())
                        .unitPrice(BigDecimal.valueOf(item.unitPrice()))
                        .build())
                .toList();

        // Monta as URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(checkout.backUrlsDto().success())
                .failure(checkout.backUrlsDto().failure())
                .pending(checkout.backUrlsDto().pending())
                .build();

        // Monta a preference
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .notificationUrl(notificationUrl)
                .externalReference(userId + "|" + packageId)
                .build();

        // Utiliza o wrapper centralizado (as exceções do SDK já estão traduzidas nele)
        Preference preference = mercadoPagoWrapper.createPreference(preferenceRequest);

        return new PaymentResult(
                preference.getId(),
                "pending_init",
                null,
                null,
                null,
                null,
                preference.getInitPoint()
        );
    }
}
