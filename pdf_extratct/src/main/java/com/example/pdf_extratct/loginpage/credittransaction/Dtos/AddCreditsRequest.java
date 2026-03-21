package com.example.pdf_extratct.loginpage.credittransaction.Dtos;

import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import jakarta.validation.constraints.*;

public record AddCreditsRequest(
        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be at least 1")
        @Max(value = 10000, message = "Amount cannot exceed 10000")
        Integer amount,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {}
