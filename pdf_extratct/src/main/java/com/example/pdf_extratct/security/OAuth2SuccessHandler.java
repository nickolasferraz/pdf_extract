package com.example.pdf_extratct.security;

import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extrair dados do Google
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");

        // Buscar ou criar usuário
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        UserEntity user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Criar novo usuário com 10 créditos de bônus
            user = new UserEntity();
            user.setEmail(email);
            user.setEmailValidado(true); // Google já validou
            user.setCreditBalance(10);   // Bônus OAuth
            user = userRepository.save(user);
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());

        // Redirecionar para frontend com token
        String redirectUrl = String.format(
                "http://localhost:3000/auth/callback?token=%s&userId=%s&email=%s&credits=%d",
                token, user.getUserId(), user.getEmail(), user.getCreditBalance()
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
