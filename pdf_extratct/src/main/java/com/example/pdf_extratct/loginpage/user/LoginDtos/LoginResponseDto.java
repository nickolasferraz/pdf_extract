package com.example.pdf_extratct.loginpage.user.LoginDtos;

public record LoginResponseDto(
        String token,
        String userId,
        String email,
        Integer creditBalance
) {
    public LoginResponseDto {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser nulo ou vazio");
        }
    }
}
