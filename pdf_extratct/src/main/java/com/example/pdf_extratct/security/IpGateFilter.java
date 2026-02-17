package com.example.pdf_extratct.security;

import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class IpGateFilter extends OncePerRequestFilter {

    private final IpBlockService ipBlockService;

    public IpGateFilter(IpBlockService ipBlockService) {
        this.ipBlockService = ipBlockService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip =extractClientIp(request);

        if(ipBlockService.isBlocked(ip)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("\"Você excedeu o uso gratutito. Faça login/crie conta para continuar.\"");
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated= auth !=null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        // Aplica apenas para rota de processamento (ajuste se houver outras rotas)
        if (!isAuthenticated && isProtectedPath(request)) {
            boolean allowed = ipBlockService.registerAnonymousUse(ip);
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain");
                response.getWriter().write("Você excedeu o uso gratutito. Faça login/crie conta para continuar.");
                return;
            }
        }

        filterChain.doFilter(request, response);


    }


    private boolean isProtectedPath(HttpServletRequest request){
        return "Post".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/pdf-to-excel");
    }


    private String extractClientIp(HttpServletRequest request){
        String xf=request.getHeader("X-Forwarded-For");

        if (xf !=null && !xf.isBlank()){
            return xf.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
