package com.example.pdf_extratct.loginpage.user.LoginDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "this Email is mandatory")
        @Email(message = "invalid email address")
        String email,

        @NotBlank(message = "password is mandatory")
        String password
) {}
