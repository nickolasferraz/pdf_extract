package com.example.pdf_extratct.Payment.dto;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.factory.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve serializar um PaymentRequest de PIX com as chaves JSON corretas (snake_case)")
    void shouldSerializePixRequestCorrectly() throws Exception {
        // Arrange
        PaymentRequest request = TestDataFactory.createValidPixRequest(10, new BigDecimal("50.00"));

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert - Verifica regras do @JsonProperty
        assertTrue(json.contains("\"transaction_amount\":50.00"), "O JSON deve conter transaction_amount snake_case");
        assertTrue(json.contains("\"package_id\":10"), "O JSON deve conter package_id snake_case");
        assertTrue(json.contains("\"paymentType\":\"PIX\""), "O Enum deve ser serializado como a string 'PIX'");
        assertTrue(json.contains("\"first_name\":\"Test\""), "O JSON do pagador deve conter first_name");
        assertTrue(json.contains("\"last_name\":\"User\""), "O JSON do pagador deve conter last_name");
        assertFalse(json.contains("cardDetails"), "Pix não deve instanciar dados do cartão");
        assertFalse(json.contains("checkoutProDetails"), "Pix não deve instanciar dados do checkout pro");
    }

    @Test
    @DisplayName("Deve desserializar um JSON de Cartão de Crédito corretamente para o objeto PaymentRequest")
    void shouldDeserializeCreditCardRequestCorrectly() throws Exception {
        // Arrange
        String jsonInput = """
                {
                   "paymentType": "CREDIT_CARD",
                   "transaction_amount": 100.50,
                   "package_id": 99,
                   "description": "Assinatura Mensal",
                   "payer": {
                     "email": "teste@email.com",
                     "first_name": "João",
                     "last_name": "Silva",
                     "identification": {
                       "type": "CPF",
                       "number": "00011122233"
                     }
                   },
                   "cardDetails": {
                     "token": "tok_abcdef123",
                     "installments": 3,
                     "payment_method_id": "master",
                     "issuerId": "333"
                   }
                 }
                """;

        // Act
        PaymentRequest request = objectMapper.readValue(jsonInput, PaymentRequest.class);

        // Assert
        assertNotNull(request);
        assertEquals(PaymentType.CREDIT_CARD, request.paymentType());
        assertEquals(new BigDecimal("100.50"), request.transactionAmount());
        assertEquals(99, request.packageId());
        
        assertNotNull(request.payer());
        assertEquals("João", request.payer().firstName());
        
        assertNotNull(request.cardDetails());
        assertEquals("tok_abcdef123", request.cardDetails().token());
        assertEquals(3, request.cardDetails().installments());
        assertEquals("master", request.cardDetails().paymentMethodId());
    }
}
