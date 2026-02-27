package com.example.pdf_extratct.Payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayerDTO {

    private String email;
    private String firstName;
    private IdentificationDTO identification;

}
