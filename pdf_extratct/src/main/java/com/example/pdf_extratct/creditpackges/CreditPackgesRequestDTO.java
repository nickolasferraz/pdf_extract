package com.example.pdf_extratct.creditpackges;

import jakarta.validation.constraints.NotNull;

public record CreditPackgesRequestDTO(
        @NotNull
        int packageId,
        @NotNull
        NamePackageEnum name,
        @NotNull
        int credits,
        @NotNull
        int price_cents,
        @NotNull
        String moeda

) {
}
