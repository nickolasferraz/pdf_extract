package com.example.pdf_extratct.Payment.client;

import com.example.pdf_extratct.Payment.models.entity.Payer;
import com.example.pdf_extratct.Payment.models.entity.PaymentEntity;
import com.example.pdf_extratct.Payment.exceptions.PaymentGatewayException;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoClient {

    private final MercadoPagoWrapper mercadoPagoWrapper;

    // Único método restante — usado pelo webhook para consultar status de um pagamento
    public PaymentEntity getPaymentStatus(long id) {
        
        // Chamada agora feita na abstração, com as MPExceptions já tratadas
        Payment payment = mercadoPagoWrapper.getPayment(id);

        String status = payment.getStatus();
        String paymentMethod = payment.getPaymentMethodId();
        log.info("Status: {} | Método: {}", status, paymentMethod);

        return PaymentEntity.builder()
                .id(payment.getId().toString())
                .orderId(payment.getExternalReference())
                .externalReference(payment.getExternalReference())
                .status(status)
                .amount(payment.getTransactionAmount() != null
                        ? payment.getTransactionAmount().toString() : null)
                .statusDetail(payment.getStatusDetail())
                .payer(buildPayerFromPayment(payment))
                .payementMethodId(paymentMethod)
                .build();
    }

    private Payer buildPayerFromPayment(Payment payment) {
        if (payment.getPayer() != null && payment.getPayer().getEmail() != null) {
            return Payer.builder()
                    .email(payment.getPayer().getEmail())
                    .firstName(payment.getPayer().getFirstName())
                    .identification(Payer.Identification.builder()
                            .type(payment.getPayer().getIdentification().getType())
                            .number(payment.getPayer().getIdentification().getNumber())
                            .build())
                    .build();
        }
        return null;
    }
}

