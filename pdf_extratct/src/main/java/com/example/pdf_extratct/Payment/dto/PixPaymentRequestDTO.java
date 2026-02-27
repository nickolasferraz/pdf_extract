package com.example.pdf_extratct.Payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record PixPaymentRequestDTO(
        @JsonProperty("transaction_amount") BigDecimal transactionAmount,
        String description,
        @JsonProperty("payment_method_id") String paymentMethodId,
        PayerDTO payer
) {
    public record PayerDTO(
            String email,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            IdentificationDTO identification
    ) {
    }

    public record IdentificationDTO(
            String type,
            String number
    ) {
    }
}
