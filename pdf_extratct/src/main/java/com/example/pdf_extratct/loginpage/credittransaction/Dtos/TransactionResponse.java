package com.example.pdf_extratct.loginpage.credittransaction.Dtos;

import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;

public record TransactionResponse(
        String id,
        TransactionType type,
        Integer amount,
        Integer balanceBefore,
        Integer balanceAfter,
        String description,
        String relatedJobId,
        java.sql.Timestamp createdAt
) {}
