package com.example.pdf_extratct.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;


    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
             OAuth2SuccessHandler oAuth2SuccessHandler

    ){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // ⚠️ Configure CORS depois
                .authorizeHttpRequests(auth -> auth
                        // 🔓 ROTAS PÚBLICAS
                        .requestMatchers("/api/auth/**").permitAll()           // Login/Registro local
                        .requestMatchers("/login/oauth2/**").permitAll()       // Callback OAuth
                        .requestMatchers("/oauth2/**").permitAll()             // Autorização OAuth
                        .requestMatchers("/api/public/**").permitAll()         // Trial sem login

                        // 🔐 ROTAS AUTENTICADAS
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Só admin
                        .anyRequest().authenticated()                           // Resto precisa login
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 👇 OAUTH2 LOGIN (Google)
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )
                // 👇 JWT FILTER (Login Local)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}