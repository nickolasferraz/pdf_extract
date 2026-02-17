package com.example.pdf_extratct.security;

import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.auth.AuthAccountRepository;
import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.credittransaction.CreditService; // Importar CreditService
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType; // Importar TransactionType
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final JwtUtil jwtUtil;
    private final CreditService creditService; // Injetar CreditService

    @Override
    @Transactional
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

            // ✅ CRIAR AuthAccountEntity para vincular com Google
            AuthAccountEntity authAccount = new AuthAccountEntity();
            authAccount.setUser(user);
            authAccount.setProvider(AuthProvider.GOOGLE);
            authAccount.setProviderId(googleId);
            authAccountRepository.save(authAccount);

            // ✅ REGISTRAR TRANSAÇÃO DE BÔNUS
            creditService.addCredits(
                    user,
                    10, // Valor do bônus
                    TransactionType.BONUS,
                    "Bônus de registro via Google OAuth"
            );
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());

        // ✅ CORRIGIDO: Redirecionar para porta 4200 (Angular)
        String redirectUrl = String.format(
                "http://localhost:4200?token=%s",
                token
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
