package com.example.pdf_extratct.Payment.enums;

import com.example.pdf_extratct.Payment.enums.PaymentType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTypeTest {

    @ParameterizedTest(name = "Deve conter o tipo de pagamento: {0}")
    @EnumSource(PaymentType.class)
    @DisplayName("Garante que os tipos oficiais de pagamento do sistema existem")
    void shouldContainExpectedPaymentTypes(PaymentType paymentType) {
        assertNotNull(paymentType, "O enum PaymentType não deveria ser nulo");
    }

    @ParameterizedTest(name = "Deve converter a String '{0}' para o Enum {1}")
    @CsvSource({
            "PIX, PIX",
            "CREDIT_CARD, CREDIT_CARD",
            "CHECKOUT_PRO, CHECKOUT_PRO"
    })
    @DisplayName("Garante que o valueOf funciona corretamente para as strings esperadas do JSON")
    void shouldConvertStringToEnum(String input, PaymentType expectedEnum) {
        PaymentType converted = PaymentType.valueOf(input);
        assertEquals(expectedEnum, converted, "A conversão via valueOf falhou");
    }

    @ParameterizedTest(name = "Deve lançar IllegalArgumentException para o tipo inválido: {0}")
    @ValueSource(strings = {"BOLETO", "DEBIT_CARD", "  ", "pix", "Credit_Card"})
    @DisplayName("Garante que tipos não mapeados ou mal formatados lançam exceção")
    void shouldThrowExceptionForInvalidPaymentTypes(String invalidInput) {
        assertThrows(IllegalArgumentException.class, () -> {
            PaymentType.valueOf(invalidInput);
        });
    }

    @Test
    @DisplayName("Garante que o número total de integrações de pagamento suportadas é exatemente 3")
    void shouldHaveExactlyThreePaymentTypes() {
        PaymentType[] values = PaymentType.values();
        assertEquals(3, values.length, "O sistema só deve suportar PIX, CREDIT_CARD e CHECKOUT_PRO no momento");
    }
}
