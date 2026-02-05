package com.example.pdf_extratct.loginpage.controllers;

import com.example.pdf_extratct.loginpage.auth.service.AuthService;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginRequestDto;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginResponseDto;
import com.example.pdf_extratct.loginpage.user.RegisterDtoRequest.UserRegisterRequestDto;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequestDto request) {
        try {
            LoginResponseDto response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            LoginResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> inforuser(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Não autenticado")
            );
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("email", user.getEmail());
        userInfo.put("creditBalance", user.getCreditBalance());
        userInfo.put("emailValidado", user.getEmailValidado());
        userInfo.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Token inválido")
            );
        }

        try {
            String newToken = jwtUtil.generateToken(user.getUserId(), user.getEmail());

            LoginResponseDto response = new LoginResponseDto(
                    newToken,
                    user.getUserId(),
                    user.getEmail(),
                    user.getCreditBalance()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Erro ao renovar token")
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT é stateless, logout acontece no frontend removendo o token
        return ResponseEntity.ok(
                Map.of("message", "Logout realizado com sucesso")
        );
    }
}
