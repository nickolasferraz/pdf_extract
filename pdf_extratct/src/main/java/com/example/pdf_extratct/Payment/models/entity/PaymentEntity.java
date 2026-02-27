package com.example.pdf_extratct.Payment.models.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEntity {
    private String id;
    private String orderId;
    private String status;
    private String amount;
    private String statusDetail;
    private String payementMethodId;
    private Payer payer;
}
