package com.example.pdf_extratct.loginpage.auth.service;

import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.user.RegisterDtoRequest.UserRegisterRequestDto;
import com.example.pdf_extratct.loginpage.user.strategy.UserCreationStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LocalUserCreationStrategy implements UserCreationStrategy {

    @Override
    public AuthProvider getProviderName() {
        return AuthProvider.LOCAL;
    }

    @Override
    public UserEntity createUser( UserRegisterRequestDto request) {
        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setEmailValidado(false);
        user.setCreditBalance(0);
        return user;
    }

    @Override
    public AuthAccountEntity createAuthAccount(UserEntity user, UserRegisterRequestDto request, PasswordEncoder encoder) {
        AuthAccountEntity authAccount = new AuthAccountEntity();
        authAccount.setUser(user);
        authAccount.setProvider(getProviderName());
        authAccount.setPasswordHash(encoder.encode(request.password()));
        return authAccount;
    }
}