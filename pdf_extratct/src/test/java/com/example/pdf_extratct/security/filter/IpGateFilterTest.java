package com.example.pdf_extratct.security.filter;

import com.example.pdf_extratct.security.config.SecurityProperties;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpGateFilterTest {

    @Mock
    private IpBlockService ipBlockService;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private IpGateFilter ipGateFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir acesso sem validar IP se a rota for pública")
    void shouldAllowPublicPath() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(securityProperties.getPublicPaths()).thenReturn(List.of("/api/auth/**"));

        ipGateFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(ipBlockService);
    }

    @Test
    @DisplayName("Deve permitir acesso sem validar IP se o usuário estiver autenticado")
    void shouldAllowAuthenticatedUser() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure/data");
        when(securityProperties.getPublicPaths()).thenReturn(List.of());
        
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of())
        );

        ipGateFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(ipBlockService);
    }

    @Test
    @DisplayName("Deve bloquear acesso (403) se o IP estiver na lista de bloqueio")
    void shouldBlockBlockedIp() throws ServletException, IOException {
        String mockIp = "192.168.1.1";
        when(request.getRequestURI()).thenReturn("/api/gated/resource");
        when(request.getRemoteAddr()).thenReturn(mockIp);
        when(securityProperties.getPublicPaths()).thenReturn(List.of());
        when(ipBlockService.isBlocked(mockIp)).thenReturn(true);
        
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        ipGateFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("Deve registrar uso anônimo em rotas monitoradas e permitir se houver cota")
    void shouldRegisterAnonymousUseAndAllowIfHasQuota() throws ServletException, IOException {
        String mockIp = "10.0.0.1";
        when(request.getRequestURI()).thenReturn("/api/pdf-to-excel");
        when(request.getRemoteAddr()).thenReturn(mockIp);
        when(securityProperties.getPublicPaths()).thenReturn(List.of());
        when(securityProperties.getIpGatedPaths()).thenReturn(List.of("/api/pdf-to-excel"));
        when(ipBlockService.isBlocked(mockIp)).thenReturn(false);
        when(ipBlockService.registerAnonymousUse(mockIp)).thenReturn(true);

        ipGateFilter.doFilter(request, response, filterChain);

        verify(ipBlockService).registerAnonymousUse(mockIp);
        verify(filterChain).doFilter(request, response);
    }
}
