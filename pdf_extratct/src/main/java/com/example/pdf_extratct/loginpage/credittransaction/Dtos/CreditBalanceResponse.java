package com.example.pdf_extratct.loginpage.credittransaction.Dtos;

public record CreditBalanceResponse(
        String userId,
        Integer balance,
        String username
) {
}
