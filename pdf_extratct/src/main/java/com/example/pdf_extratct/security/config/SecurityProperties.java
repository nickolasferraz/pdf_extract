package com.example.pdf_extratct.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * Rotas totalmente públicas, liberadas sem JWT e sem bloqueio de IP.
     */
    private List<String> publicPaths = new ArrayList<>();

    /**
     * Lista de origens permitidas para CORS.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Rotas protegidas ou específicas que são monitoradas pelo controle de cota (IpGateFilter).
     * Exemplo: /api/pdf-to-excel
     */
    private List<String> ipGatedPaths = new ArrayList<>();

    public String[] getPublicPathsArray() {
        return publicPaths.toArray(new String[0]);
    }
}
