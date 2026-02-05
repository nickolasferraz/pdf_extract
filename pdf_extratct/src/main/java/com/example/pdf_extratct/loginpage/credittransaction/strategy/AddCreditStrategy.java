package com.example.pdf_extratct.loginpage.credittransaction.strategy;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.stereotype.Component;
@Component
public class AddCreditStrategy implements CreditTransactionStrategy {

    private final TransactionType type;

    public AddCreditStrategy() {
        this.type = TransactionType.PURCHASE; // Default
    }

    @Override
    public TransactionType getTransactionType() {
        return type;
    }

    @Override
    public CreditTransactionData createTransaction(UserEntity user, Integer amount, String description, String relatedId) {
        return CreditTransactionData.forCredit(user, amount, type, description);
    }

    @Override
    public boolean validateBalance(UserEntity user, Integer amount) {
        return true; // Adicionar crédito sempre pode
    }

}
