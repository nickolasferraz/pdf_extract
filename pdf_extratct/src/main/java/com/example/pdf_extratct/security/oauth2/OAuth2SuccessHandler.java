package com.example.pdf_extratct.security.oauth2;

import com.example.pdf_extratct.security.jwt.JwtUtil;
import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.auth.AuthAccountRepository;
import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.credittransaction.querys.CreditCommandService;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final CreditCommandService creditCommandService;

    @Value("${app.security.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String COOKIE_NAME = "JWT";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24; // 1 dia


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

            // Criar AuthAccountEntity para vincular com Google
            AuthAccountEntity authAccount = new AuthAccountEntity();
            authAccount.setUser(user);
            authAccount.setProvider(AuthProvider.GOOGLE);
            authAccount.setProviderId(googleId);
            authAccountRepository.save(authAccount);

            // Registrar transação de bônus
            creditCommandService.execute(
                    TransactionType.BONUS,
                    user,
                    10,
                    "Bônus de registro via Google OAuth",
                    null
            );
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());

        // Em produção: SameSite=None; Secure (necessário para cookies cross-origin com HTTPS)
        // Em dev: SameSite=Lax sem Secure (funciona no localhost)
        boolean isProd = "prod".equals(activeProfile);
        String cookieHeader;
        if (isProd) {
            cookieHeader = String.format("%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=None; Secure",
                    COOKIE_NAME, token, COOKIE_MAX_AGE);
        } else {
            cookieHeader = String.format("%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                    COOKIE_NAME, token, COOKIE_MAX_AGE);
        }

        response.setHeader("Set-Cookie", cookieHeader);

        // Redireciona para o frontend configurado via properties
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
