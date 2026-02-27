package com.example.pdf_extratct.Payment.dto;

import com.example.pdf_extratct.Payment.models.entity.Payer;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PaymentCreateResponsetDTO {
    private final  Long id;
   private final BigDecimal transactionAmount;
    private final String token;
    private final  String description;
    private final  Integer installments;
    private final  String paymentMethodId;
    private final  Payer payer;

    public PaymentCreateResponsetDTO (Long id, BigDecimal transactionAmount,
                                      String token, String description,
                                      Integer installments,
                                      String paymentMethodId,
                                      Payer payer

    ) {
        this.id = id;
        this.transactionAmount = transactionAmount;
        this.token = token;
        this.description = description;
        this.installments = installments;
        this.paymentMethodId = paymentMethodId;
        this.payer = payer;
    }



}
