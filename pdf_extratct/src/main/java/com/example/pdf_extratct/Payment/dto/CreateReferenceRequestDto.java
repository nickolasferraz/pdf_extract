package com.example.pdf_extratct.Payment.dto;


import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

public record CreateReferenceRequestDto(

        long UserId,

        @NotNull
        int totalAmount,

        @NotNull
        int packageId,

        @NotNull
        PayerDTO payer,

        @NotNull
        BackUrlsDto backUrls,

        @NotNull
        @Validated
        List<ItemsDto> items
) {

    public  record ItemsDto(
            String id,
            String title,
            int quantity,
            int unitPrice
    ){}

    public record BackUrlsDto(
            String success,
            String failure,
            String pending
    ){}

}