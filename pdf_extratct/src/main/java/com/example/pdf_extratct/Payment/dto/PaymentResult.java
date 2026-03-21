package com.example.pdf_extratct.Payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResult
{
    String paymentId;
    String status;
    String statusDetail;
    String qrCode;
    String qrCodeBase64;
    String ticketUrl;
    String checkoutUrl;

    public PaymentResult(String paymentId, String status, String statusDetail, String qrCode, String qrCodeBase64, String ticketUrl, String checkoutUrl) {
        this.paymentId = paymentId;
        this.status = status;
        this.statusDetail = statusDetail;
        this.qrCode = qrCode;
        this.qrCodeBase64 = qrCodeBase64;
        this.ticketUrl = ticketUrl;
        this.checkoutUrl = checkoutUrl;
    }

    public PaymentResult(Object paymentId, Object status, Object statusDetail, String pending) {
    }
}
