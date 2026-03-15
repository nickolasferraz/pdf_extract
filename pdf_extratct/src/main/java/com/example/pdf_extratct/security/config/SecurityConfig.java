package com.example.pdf_extratct.security.config;
import com.example.pdf_extratct.security.filter.JwtAuthenticationFilter;
import com.example.pdf_extratct.security.filter.IpGateFilter;
import com.example.pdf_extratct.security.oauth2.OAuth2SuccessHandler;
import com.example.pdf_extratct.security.exception.AjaxAuthenticationEntryPoint;


import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final AjaxAuthenticationEntryPoint ajaxAuthenticationEntryPoint;
    private final IpBlockService ipBlockService;
    private final SecurityProperties securityProperties;
    private final Environment env;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            CorsConfigurationSource corsConfigurationSource,
            AjaxAuthenticationEntryPoint ajaxAuthenticationEntryPoint,
            IpBlockService ipBlockService,
            SecurityProperties securityProperties,
            Environment env
    ){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
        this.ajaxAuthenticationEntryPoint = ajaxAuthenticationEntryPoint;
        this.ipBlockService = ipBlockService;
        this.securityProperties = securityProperties;
        this.env = env;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        IpGateFilter ipGateFilter = new IpGateFilter(ipBlockService, securityProperties);

        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                
                // 1. Libera as rotas de cota de IP e totalmente públicas dinamicamente do YAML (Garante OCP)
                if (!securityProperties.getPublicPaths().isEmpty()) {
                    auth.requestMatchers(securityProperties.getPublicPathsArray()).permitAll();
                }
                if (!securityProperties.getIpGatedPaths().isEmpty()) {
                    auth.requestMatchers(securityProperties.getIpGatedPaths().toArray(new String[0])).permitAll();
                }

                // 2. Rotas restritas e Default
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated();
            })
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(ajaxAuthenticationEntryPoint)
            )
            .oauth2Login(oauth -> oauth
                    .successHandler(oAuth2SuccessHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(ipGateFilter, jwtAuthenticationFilter.getClass());

        // Configuração de CORS por Ambiente (Garante DRY)
        if (isDev) {
            http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        } else {
            http.cors(cors -> cors.disable());
        }

        return http.build();
    }
}

