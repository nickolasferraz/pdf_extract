package com.example.pdf_extratct.loginpage.user.RegisterDtoRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterRequestDto {

    @JsonProperty("email")
    @NotBlank(message = "this Email is mandatory")
    @Email(message = "invalid email address")
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "password is mandatory")
    @Size(min = 6, message = "password must be minimum 6 characters")
    private String password;

    // Construtor vazio (obrigatório para Jackson)
    public UserRegisterRequestDto() {
    }

    // Construtor com anotação @JsonCreator
    @JsonCreator
    public UserRegisterRequestDto(
            @JsonProperty("email") String email,
            @JsonProperty("password") String password) {
        this.email = email;
        this.password = password;
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserRegisterRequestDto{" +
                "email='" + email + '\'' +
                ", password='***'" + // Não exponha password no log
                '}';
    }
}
