package com.example.pdf_extratct.loginpage.user.strategy;


import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.user.RegisterDtoRequest.UserRegisterRequestDto;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface UserCreationStrategy {

    AuthProvider getProviderName();

    UserEntity createUser(UserRegisterRequestDto request);

    AuthAccountEntity createAuthAccount(UserEntity user, UserRegisterRequestDto request, PasswordEncoder encoder);
}