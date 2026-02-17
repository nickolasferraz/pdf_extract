package com.example.pdf_extratct.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * AuthenticationEntryPoint customizado que:
 * - Retorna 401 para requisições AJAX/API (em vez de redirecionar)
 * - Redireciona para OAuth para requisições de navegador
 */
@Component
public class AjaxAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        
        // Detecta se é uma requisição AJAX/API
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                      || request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json")
                      || request.getRequestURI().startsWith("/api/");
        
        if (isAjax) {
            // Para requisições AJAX: retorna 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        } else {
            // Para requisições de navegador: redireciona para OAuth
            response.sendRedirect("/oauth2/authorization/google");
        }
    }
}
