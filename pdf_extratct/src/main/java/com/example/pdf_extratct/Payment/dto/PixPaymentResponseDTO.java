package com.example.pdf_extratct.Payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PixPaymentResponseDTO(
        Long id,
        String status,
        @JsonProperty("status_detail") String statusDetail,
        @JsonProperty("point_of_interaction") PointOfInteractionDTO pointOfInteraction
) {
    public record PointOfInteractionDTO(
            @JsonProperty("transaction_data") TransactionDataDTO transactionData
    ) {
    }

    public record TransactionDataDTO(
            @JsonProperty("qr_code") String qrCode,
            @JsonProperty("qr_code_base64") String qrCodeBase64,
            @JsonProperty("ticket_url") String ticketUrl
    ) {
    }
}
