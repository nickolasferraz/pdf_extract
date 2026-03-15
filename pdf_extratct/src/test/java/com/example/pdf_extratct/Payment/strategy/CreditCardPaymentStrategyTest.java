package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardPaymentStrategyTest {

    @Mock
    private MercadoPagoWrapper wrapper;

    @InjectMocks
    private CreditCardPaymentStrategy strategy;

    @Captor
    private ArgumentCaptor<PaymentCreateRequest> requestCaptor;

    private PaymentRequest validCCRequest;
    private final String MOCK_USER_ID = "usr-002";
    private final Integer MOCK_PACKAGE_ID = 20;
    private final String NOTIFICATION_URL = "http://localhost:8080/webhook";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(strategy, "notificationUrl", NOTIFICATION_URL);
        validCCRequest = TestDataFactory.createValidCreditCardRequest(MOCK_PACKAGE_ID, new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve inicializar a strategy corretamente informando o tipo CREDIT_CARD")
    void shouldReturnCreditCardPaymentType() {
        assertEquals(PaymentType.CREDIT_CARD, strategy.getType());
    }

    @Test
    @DisplayName("Deve lançar exceção se os detalhes do cartão não forem enviados na request")
    void shouldThrowExceptionIfCardDetailsIsMissing() {
        PaymentRequest invalidRequest = TestDataFactory.createValidPixRequest(10, BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            strategy.execute(invalidRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
        });

        assertEquals("cardDetails é obrigatório para CREDIT_CARD", ex.getMessage());
        verifyNoInteractions(wrapper); // API não deve ser chamada
    }

    @Test
    @DisplayName("Deve montar o objeto do Mercado Pago com parcelas e token do cartão e retornar approved")
    void shouldExecuteCreditCardPaymentSuccessfully() {
        // Arrange
        Payment mockPaymentResponse = mock(Payment.class);
        when(mockPaymentResponse.getId()).thenReturn(987654321L);
        when(mockPaymentResponse.getStatus()).thenReturn("approved");
        when(mockPaymentResponse.getStatusDetail()).thenReturn("accredited");

        when(wrapper.createPayment(any(PaymentCreateRequest.class), any(MPRequestOptions.class)))
                .thenReturn(mockPaymentResponse);

        // Act
        PaymentResult result = strategy.execute(validCCRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);

        // Assert - Resposta
        assertNotNull(result);
        assertEquals("987654321", result.getPaymentId());
        assertEquals("approved", result.getStatus());
        assertEquals("accredited", result.getStatusDetail());
        assertNull(result.getQrCode(), "Cartão não tem PIX QR Code");

        // Assert - Request Montado para o SDK
        verify(wrapper).createPayment(requestCaptor.capture(), any(MPRequestOptions.class));
        PaymentCreateRequest capturedRequest = requestCaptor.getValue();
        
        assertEquals(new BigDecimal("150.00"), capturedRequest.getTransactionAmount());
        assertEquals("TEST-fake-card-token", capturedRequest.getToken());
        assertEquals(1, capturedRequest.getInstallments());
        assertEquals("master", capturedRequest.getPaymentMethodId());
        assertEquals(TestDataFactory.FAKE_DESCRIPTION, capturedRequest.getDescription());
        assertEquals("usr-002|20", capturedRequest.getExternalReference());
    }
}
