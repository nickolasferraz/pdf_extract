package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.MercadoPagoConfigDTO;
import com.example.pdf_extratct.Payment.dto.ProcessNotificationResponseDTO;
import com.example.pdf_extratct.logging.ApiLogRepository;
import com.example.pdf_extratct.security.jwt.JwtUtil;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.security.config.SecurityProperties;
import com.example.pdf_extratct.security.oauth2.OAuth2SuccessHandler;
import com.example.pdf_extratct.security.exception.AjaxAuthenticationEntryPoint;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import com.example.pdf_extratct.Payment.service.ProcessPaymentNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessPaymentNotificationService notificationService;

    @MockBean
    private ApiLogRepository apiLogRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private IpBlockService ipBlockService;

    @MockBean
    private SecurityProperties securityProperties;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private AjaxAuthenticationEntryPoint ajaxAuthenticationEntryPoint;

    @MockBean
    private StorageService storageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 200 OK quando o webhook for processado com sucesso")
    void shouldReturn200OkWhenWebhookIsProcessedSuccessfully() throws Exception {
        MercadoPagoConfigDTO payload = new MercadoPagoConfigDTO();
        payload.setType("payment");
        
        MercadoPagoConfigDTO.MercadoPagoData data = new MercadoPagoConfigDTO.MercadoPagoData();
        data.setId("123456789");
        payload.setData(data);

        ProcessNotificationResponseDTO successResponse = new ProcessNotificationResponseDTO(true, "Mock message");
        when(notificationService.processNotification("123456789", "payment")).thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error caso o service dispare uma exceção")
    void shouldReturn500WhenServiceThrowsException() throws Exception {
        MercadoPagoConfigDTO payload = new MercadoPagoConfigDTO();
        payload.setType("payment");
        
        MercadoPagoConfigDTO.MercadoPagoData data = new MercadoPagoConfigDTO.MercadoPagoData();
        data.setId("987654321");
        payload.setData(data);

        when(notificationService.processNotification(anyString(), anyString()))
                .thenThrow(new RuntimeException("Banco de dados indisponível"));

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError());
    }
}
