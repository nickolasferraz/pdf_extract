package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.service.PaymentService;
import com.example.pdf_extratct.logging.ApiLogRepository;
import com.example.pdf_extratct.security.jwt.JwtUtil;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.security.config.SecurityProperties;
import com.example.pdf_extratct.security.oauth2.OAuth2SuccessHandler;
import com.example.pdf_extratct.security.exception.AjaxAuthenticationEntryPoint;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreatePaymentController.class)
@ActiveProfiles("test")
public class CreatePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

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

    private UserEntity mockAuthUser;

    @BeforeEach
    void setUp() {
        mockAuthUser = new UserEntity();
        mockAuthUser.setUserId(UUID.randomUUID().toString());
        mockAuthUser.setEmail("test@user.com");
        
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(mockAuthUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testProcessPayment() throws Exception {
        PaymentRequest request = new PaymentRequest(
                PaymentType.CREDIT_CARD,
                new BigDecimal("100.00"),
                1,
                "Test Payment",
                null, 
                null, 
                null  
        );

        PaymentResult result = new PaymentResult(null, null, null, "pending");

        when(paymentService.processPayment(any(PaymentRequest.class), any(String.class), anyInt())).thenReturn(result);

        mockMvc.perform(post("/api/v1/payments/pay")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
