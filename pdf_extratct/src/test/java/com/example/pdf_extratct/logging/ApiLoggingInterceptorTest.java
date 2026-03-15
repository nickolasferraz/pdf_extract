package com.example.pdf_extratct.logging;

import com.example.pdf_extratct.BaseIntegrationTest;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiLoggingInterceptorTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiLogRepository apiLogRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        apiLogRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setUserId(UUID.randomUUID().toString());
        testUser.setEmail("logger@test.com");
        testUser.setPassword("pass");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Deve salvar log de API no MongoDB após uma requisição autenticada")
    void shouldPersistLogAfterAuthenticatedRequest() throws Exception {
        String token = generateTestJwt(testUser.getUserId(), testUser.getEmail());

        // Faz uma requisição para um endpoint qualquer (ex: listagem de pacotes)
        mockMvc.perform(get("/api/credit-packages")
                        .cookie(new Cookie("JWT", token)))
                .andExpect(status().isOk());

        // Verifica se o log foi salvo
        List<ApiLogDocument> logs = apiLogRepository.findAll();
        assertFalse(logs.isEmpty(), "Deve existir pelo menos um log");
        
        ApiLogDocument log = logs.get(0);
        assertEquals("/api/credit-packages", log.getEndpoint());
        assertEquals("GET", log.getMethod());
        assertEquals(200, log.getStatusCode());
        assertEquals(testUser.getUserId(), log.getUserId());
        assertEquals(testUser.getEmail(), log.getUserEmail());
        assertNotNull(log.getResponseTimeMs());
    }

    @Test
    @DisplayName("Deve salvar log mesmo para requisições anônimas")
    void shouldPersistLogForAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/credit-packages"))
                .andExpect(status().isOk());

        List<ApiLogDocument> logs = apiLogRepository.findAll();
        assertFalse(logs.isEmpty());

        ApiLogDocument log = logs.get(0);
        assertNull(log.getUserId(), "UserId deve ser nulo para anônimo");
        assertEquals(200, log.getStatusCode());
    }
}
