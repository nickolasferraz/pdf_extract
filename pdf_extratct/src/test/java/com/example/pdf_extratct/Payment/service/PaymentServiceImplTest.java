package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.exceptions.PaymentGatewayException;
import com.example.pdf_extratct.Payment.factory.PaymentStrategyFactory;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.example.pdf_extratct.Payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentStrategyFactory factory;

    @Mock
    private PaymentStrategy mockedStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest validRequest;
    private final String MOCK_USER_ID = "user-123";
    private final Integer MOCK_PACKAGE_ID = 5;

    @BeforeEach
    void setUp() {
        validRequest = TestDataFactory.createValidPixRequest(MOCK_PACKAGE_ID, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Deve processar um pagamento redirecionando para a Strategy correta baseada no enum")
    void shouldProcessPaymentRoutingToCorrectStrategy() {
        // Arrange
        PaymentResult expectedResult = TestDataFactory.createPixPendingResult("id-123");
        
        // Configuramos a factory para retornar a nossa strategy mockada para PIX
        when(factory.getStrategy(PaymentType.PIX)).thenReturn(mockedStrategy);
        
        // Configuramos a strategy mockada para retornar o PaymentResult esperado
        when(mockedStrategy.execute(validRequest, MOCK_USER_ID, MOCK_PACKAGE_ID)).thenReturn(expectedResult);

        // Act
        PaymentResult actualResult = paymentService.processPayment(validRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);

        // Assert
        assertEquals(expectedResult, actualResult, "O resultado retornado deve ser o mesmo da strategy executada");
        
        // Verifica se os métodos foram realmente acionados uma e apenas uma vez com os parâmetros exatos
        verify(factory, times(1)).getStrategy(PaymentType.PIX);
        verify(mockedStrategy, times(1)).execute(validRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
    }

    @Test
    @DisplayName("Deve repassar a PaymentGatewayException quando a Strategy falhar")
    void shouldPropagateGatewayExceptionWhenStrategyFails() {
        // Arrange
        when(factory.getStrategy(PaymentType.PIX)).thenReturn(mockedStrategy);
        
        // Simulamos o MercadoPagoWrapper interno da strategy lançando o erro customizado
        when(mockedStrategy.execute(any(), any(), any())).thenThrow(
                new PaymentGatewayException("Erro de Gateway Mockado")
        );

        // Act & Assert
        PaymentGatewayException exception = assertThrows(PaymentGatewayException.class, () -> {
            paymentService.processPayment(validRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
        });

        assertEquals("Erro de Gateway Mockado", exception.getMessage());
        
        verify(factory).getStrategy(PaymentType.PIX);
        verify(mockedStrategy).execute(validRequest, MOCK_USER_ID, MOCK_PACKAGE_ID);
    }
}
