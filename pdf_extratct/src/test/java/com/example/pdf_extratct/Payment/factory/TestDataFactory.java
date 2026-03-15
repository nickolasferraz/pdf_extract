package com.example.pdf_extratct.Payment.factory;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Factory estática para fornecer objetos de teste limpos e consistentes
 * em toda a nossa suíte de testes.
 */
public class TestDataFactory {

    public static final String FAKE_EMAIL = "test_user@testuser.com";
    public static final String FAKE_FIRST_NAME = "Test";
    public static final String FAKE_LAST_NAME = "User";
    public static final String FAKE_CPF = "12345678909";
    public static final String FAKE_DESCRIPTION = "Pacote de Creditos PDF Extract";

    // ==========================================
    // PAYER (Usado no Request)
    // ==========================================
    public static PaymentRequest.PayerDTO createValidPayerDTO() {
        return new PaymentRequest.PayerDTO(
                FAKE_EMAIL,
                FAKE_FIRST_NAME,
                FAKE_LAST_NAME,
                new PaymentRequest.IdentificationDTO("CPF", FAKE_CPF)
        );
    }

    // ==========================================
    // PAYMENT REQUESTS
    // ==========================================

    public static PaymentRequest createValidPixRequest(Integer packageId, BigDecimal amount) {
        return new PaymentRequest(
                PaymentType.PIX,
                amount,
                packageId,
                FAKE_DESCRIPTION,
                createValidPayerDTO(),
                null,
                null
        );
    }

    public static PaymentRequest createValidCreditCardRequest(Integer packageId, BigDecimal amount) {
        return new PaymentRequest(
                PaymentType.CREDIT_CARD,
                amount,
                packageId,
                FAKE_DESCRIPTION,
                createValidPayerDTO(),
                new PaymentRequest.CardDetails("TEST-fake-card-token", 1, "master", "123"),
                null
        );
    }

    public static PaymentRequest createValidCheckoutProRequest(Integer packageId, BigDecimal amount) {
        return new PaymentRequest(
                PaymentType.CHECKOUT_PRO,
                amount,
                packageId,
                FAKE_DESCRIPTION,
                createValidPayerDTO(),
                null,
                new PaymentRequest.CheckoutProDetails(
                        List.of(new PaymentRequest.ItemsDto(
                                packageId.toString(),
                                FAKE_DESCRIPTION,
                                1,
                                amount.intValue()
                        )),
                        new PaymentRequest.BackUrlsDto(
                                "http://localhost:4200/success",
                                "http://localhost:4200/failure",
                                "http://localhost:4200/pending"
                        )
                )
        );
    }

    // ==========================================
    // PAYMENT RESULTS
    // ==========================================

    public static PaymentResult createApprovedResult(String paymentId) {
        return new PaymentResult(
                paymentId,
                "approved",
                "accredited",
                null,
                null,
                null,
                null
        );
    }

    public static PaymentResult createPixPendingResult(String paymentId) {
        return new PaymentResult(
                paymentId,
                "pending",
                "pending_waiting_transfer",
                "00020126360014BR.GOV.BCB.PIX...",
                "BASE64_QR_CODE_MOCK",
                "https://mercadopago.com/ticket_mock",
                null
        );
    }

    public static PaymentResult createCheckoutProResult(String preferenceId) {
        return new PaymentResult(
                preferenceId,
                "pending_init",
                null,
                null,
                null,
                null,
                "https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=" + preferenceId
        );
    }

    // ==========================================
    // ENTITIES (Para Mock de DB / JPA)
    // ==========================================

    public static UserEntity createValidUserEntity() {
        UserEntity user = new UserEntity();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail(FAKE_EMAIL);
        user.setCreditBalance(100);
        return user;
    }

    public static CreditTransactionEntity createValidTransactionEntity(UserEntity user) {
        CreditTransactionEntity tx = new CreditTransactionEntity();
        tx.setTransactionId(String.valueOf(1L));
        tx.setUser(user);
        tx.setAmount(10);
        tx.setType(TransactionType.PURCHASE);
        tx.setBalanceBefore(90);
        tx.setBalanceAfter(100);
        tx.setDescription(FAKE_DESCRIPTION);
        tx.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return tx;
    }
}
