package com.example.pdf_extratct.security.filter;
import com.example.pdf_extratct.security.config.SecurityProperties;


import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class IpGateFilter extends OncePerRequestFilter {

    private final IpBlockService ipBlockService;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public IpGateFilter(IpBlockService ipBlockService, SecurityProperties securityProperties) {
        this.ipBlockService = ipBlockService;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. Verifica se a URI é pública e não deve ser contabilizada/bloqueada (Garante OCP)
        boolean isPublic = securityProperties.getPublicPaths().stream()
                .anyMatch(p -> pathMatcher.match(p, uri));
        
        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

        // 2. Se está logado, as cotas de IP não se aplicam (passa direto)
        if (isAuthenticated) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = extractClientIp(request);
        
        if (ipBlockService.isBlocked(ip)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("Você excedeu o uso gratuito. Faça login/crie conta para continuar.");
            return;
        }

        // 3. Verifica se a rota atual precisa ser contabilizada no anonimato (Garante OCP)
        boolean isGatedPath = securityProperties.getIpGatedPaths().stream()
                .anyMatch(p -> pathMatcher.match(p, uri));

        if (isGatedPath) {
            boolean allowed = ipBlockService.registerAnonymousUse(ip);
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain");
                response.getWriter().write("Você excedeu o uso gratuito. Faça login/crie conta para continuar.");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request){
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()){
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

