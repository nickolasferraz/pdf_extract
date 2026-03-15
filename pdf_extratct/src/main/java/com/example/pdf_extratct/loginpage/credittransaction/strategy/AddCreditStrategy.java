package com.example.pdf_extratct.loginpage.credittransaction.strategy;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AddCreditStrategy implements CreditTransactionStrategy {
    @Override
    public CreditTransactionData createTransaction(UserEntity user, Integer amount, String description, String relatedId) {
        return CreditTransactionData.forCredit(user, amount, TransactionType.PURCHASE, description, relatedId);
    }

    @Override
    public boolean validateBalance(UserEntity user, Integer amount) {
        return true; // Compras são sempre válidas
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.PURCHASE;
    }
}
