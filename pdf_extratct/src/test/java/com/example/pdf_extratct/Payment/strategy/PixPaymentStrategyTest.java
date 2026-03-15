package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.exceptions.PaymentGatewayException;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentPointOfInteraction;
import com.mercadopago.resources.payment.PaymentTransactionData;
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
class PixPaymentStrategyTest {

    @Mock
    private MercadoPagoWrapper wrapper;

    @InjectMocks
    private PixPaymentStrategy strategy;

    @Captor
    private ArgumentCaptor<PaymentCreateRequest> requestCaptor;

    private PaymentRequest validPixRequest;
    private final String MOCK_USER_ID = "usr-001";
    private final Integer MOCK_PACKAGE_ID = 10;
    private final String NOTIFICATION_URL = "http://localhost:8080/webhook";

    @BeforeEach
    void setUp() {
        // Simula a injeção do @Value que o Spring faria
        ReflectionTestUtils.setField(strategy, "notificationUrl", NOTIFICATION_URL);
        validPixRequest = TestDataFactory.createValidPixRequest(MOCK_PACKAGE_ID, new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Deve inicializar a strategy corretamente informando o tipo PIX")
    void shouldReturnPixPaymentType() {
        assertEquals(PaymentType.PIX, strategy.getType());
    }

    @Test
    @DisplayName("Deve montar o objeto do Mercado Pago com os dados corretos e retornar pending")
    void shouldExecutePixPaymentSuccessfully() {
        // Arrange
        Payment mockPaymentResponse = mock(Payment.class);
        when(mockPaymentResponse.getId()).thenReturn(123456789L);
        when(mockPaymentResponse.getStatus()).thenReturn("pending");
        when(mockPaymentResponse.getStatusDetail()).thenReturn("pending_waiting_transfer");
        
        // Mocking the Point of Interaction (QR Code data)
        PaymentPointOfInteraction mockPoi = mock(PaymentPointOfInteraction.class);
        PaymentTransactionData mockTxData = mock(PaymentTransactionData.class);
        when(mockTxData.getQrCode()).thenReturn("00020126360014BR.GOV.BCB.PIX...");
        when(mockTxData.getQrCodeBase64()).thenReturn("BASE64==");
        when(mockTxData.getTicketUrl()).thenReturn("http://mp.com/ticket");
        when(mockPoi.getTransactionData()).thenReturn(mockTxData);
        when(mockPaymentResponse.getPointOfInteraction()).thenReturn(mockPoi);

        // Simulamos a criação no wrapper (API não é chamada)
        when(wrapper.createPayment(any(PaymentCreateRequest.class), any(MPRequestOptions.class)))
                .thenReturn(mockPaymentResponse);

        // Act
        PaymentResult result = strategy.execute(validPixRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);

        // Assert - Verifica o objeto de retorno
        assertNotNull(result);
        assertEquals("123456789", result.getPaymentId());
        assertEquals("pending", result.getStatus());
        assertEquals("00020126360014BR.GOV.BCB.PIX...", result.getQrCode());
        assertEquals("BASE64==", result.getQrCodeBase64());
        assertEquals("http://mp.com/ticket", result.getTicketUrl());
        assertNull(result.getCheckoutUrl(), "Checkout URL só deve existir no Checkout Pro");

        // Assert - Verifica se os dados enviados para o Wrapper foram traduzidos corretamente do nosso DTO
        verify(wrapper).createPayment(requestCaptor.capture(), any(MPRequestOptions.class));
        PaymentCreateRequest capturedRequest = requestCaptor.getValue();
        
        assertEquals(new BigDecimal("50.00"), capturedRequest.getTransactionAmount());
        assertEquals("pix", capturedRequest.getPaymentMethodId());
        assertEquals(NOTIFICATION_URL, capturedRequest.getNotificationUrl());
        assertEquals("usr-001|10", capturedRequest.getExternalReference(), "Deve montar external reference concatenada");
        assertEquals(TestDataFactory.FAKE_EMAIL, capturedRequest.getPayer().getEmail());
        assertEquals("12345678909", capturedRequest.getPayer().getIdentification().getNumber(), "O CPF deve ter sido limpo de pontuações");
    }

    @Test
    @DisplayName("Garante que se o Gateway falhar, a exception sobe intocada")
    void shouldPropagateExceptionWhenWrapperFails() {
        // Arrange
        when(wrapper.createPayment(any(), any()))
                .thenThrow(new PaymentGatewayException("Gateway Fora do Ar"));

        // Act & Assert
        PaymentGatewayException ex = assertThrows(PaymentGatewayException.class, () -> {
            strategy.execute(validPixRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
        });
        
        assertEquals("Gateway Fora do Ar", ex.getMessage());
    }
}
