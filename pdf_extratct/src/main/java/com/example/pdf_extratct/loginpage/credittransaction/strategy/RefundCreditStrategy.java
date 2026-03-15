package com.example.pdf_extratct.loginpage.credittransaction.strategy;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class RefundCreditStrategy implements CreditTransactionStrategy {
    @Override
    public CreditTransactionData createTransaction(UserEntity user, Integer amount, String description, String relatedId) {
        return CreditTransactionData.forCredit(user, amount, TransactionType.REFUND, description, relatedId);
    }

    @Override
    public boolean validateBalance(UserEntity user, Integer amount) {
        return true; // Reembolso é sempre válido
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.REFUND;
    }
}
