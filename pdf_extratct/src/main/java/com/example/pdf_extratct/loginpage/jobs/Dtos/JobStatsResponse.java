package com.example.pdf_extratct.loginpage.jobs.Dtos;

public record JobStatsResponse(
        Long total,
        Long completed,
        Long failed,
        Long pending,
        Long canceled, // Adicionado
        Long refunded, // Adicionado
        Integer totalCreditsUsed
) {}
