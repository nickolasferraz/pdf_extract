package com.example.pdf_extratct.loginpage.user.RegisterDtoRequest;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(

    @NotBlank(message = "this Email is mandatory")
    @Email(message = "invalid email address")
    String email,

    @NotBlank(message = " password is mandatory")
    @Size(message = "password must be minimum 6 characters")
     String password

){}
