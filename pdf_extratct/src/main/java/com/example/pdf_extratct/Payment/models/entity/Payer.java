package com.example.pdf_extratct.Payment.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payer {
    private String email;
    private String firstName;
    private Identification identification;

    @Data
    @Builder
    public static class Identification{
        private String type;
        private String number;
    }
}
