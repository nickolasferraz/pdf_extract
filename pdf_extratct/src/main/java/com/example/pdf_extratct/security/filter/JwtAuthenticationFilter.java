package com.example.pdf_extratct.security.filter;
import com.example.pdf_extratct.security.jwt.JwtUtil;


import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        // 1) Tentar ler do cookie httpOnly "JWT"
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    log.debug("Token JWT encontrado no cookie.");
                    break;
                }
            }
        }

        // 2) Fallback: ler do header Authorization: Bearer (compatibilidade)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.debug("Token JWT encontrado no header 'Authorization'.");
            }
        }

        if (token == null) {
            log.debug("Nenhum token JWT encontrado na requisição para a URI: {}", request.getRequestURI());
        }

        // 3) Validar e autenticar
        if (token != null) {
            if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                log.debug("Token válido para o userId: {}", userId);

                UserEntity user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    log.debug("Usuário encontrado no banco de dados: {}", user.getEmail());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Usuário autenticado com sucesso e SecurityContext atualizado.");
                } else {
                    log.warn("Usuário com ID {} não encontrado no banco de dados, mas o token é válido.", userId);
                }
            } else {
                log.warn("Token JWT inválido ou expirado.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
