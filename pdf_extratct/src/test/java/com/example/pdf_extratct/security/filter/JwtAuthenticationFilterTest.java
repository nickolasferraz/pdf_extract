package com.example.pdf_extratct.security.filter;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.security.config.SecurityConfig;
import com.example.pdf_extratct.security.config.SecurityProperties;
import com.example.pdf_extratct.security.jwt.JwtUtil;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.security.oauth2.OAuth2SuccessHandler;
import com.example.pdf_extratct.security.exception.AjaxAuthenticationEntryPoint;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import com.example.pdf_extratct.logging.ApiLogRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JwtAuthenticationFilterTest.TestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtUtil.class, AjaxAuthenticationEntryPoint.class, SecurityProperties.class})
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private IpBlockService ipBlockService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    @MockBean
    private StorageService storageService;

    @MockBean
    private ApiLogRepository apiLogRepository;

    @RestController
    public static class TestController {
        @GetMapping("/api/test-auth")
        public String test() {
            return "ok";
        }
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via Cookie 'JWT'")
    void shouldAuthenticateViaCookie() throws Exception {
        String token = "valid-token";
        String userId = "user-123";
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setEmail("test@test.com");

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/test-auth")
                        .cookie(new Cookie("JWT", token)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via Header 'Authorization' (Fallback)")
    void shouldAuthenticateViaHeader() throws Exception {
        String token = "valid-token";
        String userId = "user-123";
        UserEntity user = new UserEntity();
        user.setUserId(userId);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/test-auth")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 401/Unauthorized se o token estiver expirado")
    void shouldFailIfTokenExpired() throws Exception {
        String token = "expired-token";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isTokenExpired(token)).thenReturn(true);

        mockMvc.perform(get("/api/test-auth")
                        .cookie(new Cookie("JWT", token)))
                .andExpect(status().isUnauthorized()); 
    }

    @Test
    @DisplayName("Deve retornar 401 se o usuário do token não existir no banco")
    void shouldFailIfUserNotFound() throws Exception {
        String token = "valid-token";
        String userId = "ghost-user";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/test-auth")
                        .cookie(new Cookie("JWT", token)))
                .andExpect(status().isUnauthorized());
    }
}
