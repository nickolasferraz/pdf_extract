package com.example.pdf_extratct.Payment.strategy;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.client.MercadoPagoWrapper;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
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
class CheckoutProPaymentStrategyTest {

    @Mock
    private MercadoPagoWrapper wrapper;

    @InjectMocks
    private CheckoutProPaymentStrategy strategy;

    @Captor
    private ArgumentCaptor<PreferenceRequest> requestCaptor;

    private PaymentRequest validCheckoutRequest;
    private final String MOCK_USER_ID = "usr-003";
    private final Integer MOCK_PACKAGE_ID = 30;
    private final String NOTIFICATION_URL = "http://localhost:8080/webhook";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(strategy, "notificationUrl", NOTIFICATION_URL);
        validCheckoutRequest = TestDataFactory.createValidCheckoutProRequest(MOCK_PACKAGE_ID, new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("Deve inicializar a strategy corretamente informando o tipo CHECKOUT_PRO")
    void shouldReturnCheckoutProPaymentType() {
        assertEquals(PaymentType.CHECKOUT_PRO, strategy.getType());
    }

    @Test
    @DisplayName("Deve lançar exceção se os itens do pedido não forem enviados na request")
    void shouldThrowExceptionIfCheckoutItemsAreMissing() {
        // Envia um request válido para pix, que não tem checkoutProDetails por padrão
        PaymentRequest invalidRequest = TestDataFactory.createValidPixRequest(10, BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            strategy.execute(invalidRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
        });

        assertEquals("checkoutProDetails com items é obrigatório para CHECKOUT_PRO", ex.getMessage());
        verifyNoInteractions(wrapper); // API não deve ser chamada
    }

    @Test
    @DisplayName("Deve montar a preference do Mercado Pago com itens e URLs de retorno corretamente")
    void shouldExecuteCheckoutProPaymentSuccessfully() {
        // Arrange
        Preference mockPreferenceResponse = mock(Preference.class);
        when(mockPreferenceResponse.getId()).thenReturn("pref-987");
        when(mockPreferenceResponse.getInitPoint()).thenReturn("https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=pref-987");

        when(wrapper.createPreference(any(PreferenceRequest.class)))
                .thenReturn(mockPreferenceResponse);

        // Act
        PaymentResult result = strategy.execute(validCheckoutRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);

        // Assert - Resposta
        assertNotNull(result);
        assertEquals("pref-987", result.getPaymentId());
        assertEquals("pending_init", result.getStatus());
        assertNotNull(result.getCheckoutUrl(), "Init point não deve ser nulo");
        assertTrue(result.getCheckoutUrl().contains("pref-987"), "Init point deve conter o pref_id");
        assertNull(result.getQrCode(), "Checkout Pro não gera QR Code direto na resposta");

        // Assert - Request Montado para o SDK
        verify(wrapper).createPreference(requestCaptor.capture());
        PreferenceRequest capturedRequest = requestCaptor.getValue();
        
        assertNotNull(capturedRequest.getItems());
        assertEquals(1, capturedRequest.getItems().size());
        assertEquals(MOCK_PACKAGE_ID.toString(), capturedRequest.getItems().get(0).getId());
        assertEquals(1, capturedRequest.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(120), capturedRequest.getItems().get(0).getUnitPrice());

        // Assert - BackUrls
        assertNotNull(capturedRequest.getBackUrls());
        assertEquals("http://localhost:4200/success", capturedRequest.getBackUrls().getSuccess());
        assertEquals("http://localhost:4200/failure", capturedRequest.getBackUrls().getFailure());
        
        assertEquals(NOTIFICATION_URL, capturedRequest.getNotificationUrl());
        assertEquals("usr-003|30", capturedRequest.getExternalReference());
    }
}
