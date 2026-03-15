package com.example.pdf_extratct.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração CORS para permitir requisições do frontend Angular
 */
@Configuration
public class CorsConfig {

    private final SecurityProperties securityProperties;

    public CorsConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir origens do frontend (dinâmico via properties)
        if (securityProperties.getAllowedOrigins() != null && !securityProperties.getAllowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());
        } else {
            configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080"));
        }
        
        // Permitir todos os métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Permitir todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciais (cookies, headers de autenticação)
        configuration.setAllowCredentials(true);
        
        // Expor headers de resposta
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type",
            "X-Total-Count"
        ));
        
        // Aplicar a todas as rotas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
