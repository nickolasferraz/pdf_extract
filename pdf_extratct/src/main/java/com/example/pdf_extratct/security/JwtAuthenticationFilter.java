package com.example.pdf_extratct.security;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
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

        // ═══════════════════════════════════════════════════════════
        // CÓDIGO ANTIGO (só lia do header Authorization: Bearer)
        // ═══════════════════════════════════════════════════════════
        // String authHeader = request.getHeader("Authorization");
        //
        // if (authHeader != null && authHeader.startsWith("Bearer ")) {
        //     String token = authHeader.substring(7);
        //
        //     if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
        //         String userId = jwtUtil.getUserIdFromToken(token);
        //
        //         UserEntity user = userRepository.findById(userId).orElse(null);
        //
        //         if (user != null) {
        //             UsernamePasswordAuthenticationToken authentication =
        //                     new UsernamePasswordAuthenticationToken(
        //                             user,
        //                             null,
        //                             new ArrayList<>()
        //                     );
        //
        //             authentication.setDetails(
        //                     new WebAuthenticationDetailsSource().buildDetails(request)
        //             );
        //
        //             SecurityContextHolder.getContext().setAuthentication(authentication);
        //         }
        //     }
        // }
        // ═══════════════════════════════════════════════════════════

        // ═══════════════════════════════════════════════════════════
        // CÓDIGO NOVO (lê do cookie JWT + fallback para header Bearer)
        // ═══════════════════════════════════════════════════════════
        String token = null;

        // 1) Tentar ler do cookie httpOnly "JWT"
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2) Fallback: ler do header Authorization: Bearer (compatibilidade)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 3) Validar e autenticar
        if (token != null && jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
            String userId = jwtUtil.getUserIdFromToken(token);
            UserEntity user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}