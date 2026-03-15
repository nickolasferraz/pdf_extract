package com.example.pdf_extratct.Payment.e2e;

import com.example.pdf_extratct.BaseIntegrationTest;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentPointOfInteraction;
import com.mercadopago.resources.payment.PaymentTransactionData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentE2ETest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private MercadoPagoWrapper mercadoPagoWrapper;

    private UserEntity loggedUser;

    @BeforeEach
    void setUp() {
        loggedUser = new UserEntity();
        loggedUser.setEmail("e2e@test.com");
        loggedUser.setPassword("password123");
        loggedUser = userRepository.save(loggedUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Fluxo E2E: Deve receber request PIX na API, acionar Factory/Strategy real e retornar Status 201 via Mock HTTP")
    void executeFullPixPaymentFlow() throws Exception {
        PaymentRequest pixRequest = TestDataFactory.createValidPixRequest(100, new BigDecimal("60.00"));

        Payment mockMPResponse = mock(Payment.class);
        when(mockMPResponse.getId()).thenReturn(102030L);
        when(mockMPResponse.getStatus()).thenReturn("pending");
        when(mockMPResponse.getStatusDetail()).thenReturn("pending_waiting_transfer");
        
        PaymentPointOfInteraction mockPoi = mock(PaymentPointOfInteraction.class);
        PaymentTransactionData mockTxData = mock(PaymentTransactionData.class);
        when(mockTxData.getQrCodeBase64()).thenReturn("IMAGE_BASE64_CODE");
        when(mockPoi.getTransactionData()).thenReturn(mockTxData);
        when(mockMPResponse.getPointOfInteraction()).thenReturn(mockPoi);

        when(mercadoPagoWrapper.createPayment(any(PaymentCreateRequest.class), any(MPRequestOptions.class)))
                .thenReturn(mockMPResponse);

        mockMvc.perform(post("/api/v1/payments/pay")
                .cookie(new Cookie("JWT", generateTestJwt(loggedUser.getUserId(), loggedUser.getEmail())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pixRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("102030"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.statusDetail").value("pending_waiting_transfer"))
                .andExpect(jsonPath("$.qrCodeBase64").value("IMAGE_BASE64_CODE"));
    }
}
