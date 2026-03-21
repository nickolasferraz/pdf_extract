package com.example.pdf_extratct.loginpage.credittransaction.strategy;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DebitCreditStrategy implements CreditTransactionStrategy {
    @Override
    public CreditTransactionData createTransaction(UserEntity user, Integer amount, String description, String relatedId) {
        return CreditTransactionData.forDebit(user, amount, relatedId, description);
    }

    @Override
    public boolean validateBalance(UserEntity user, Integer amount) {
        return user.getCreditBalance() >= amount;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.USAGE;
    }
}
