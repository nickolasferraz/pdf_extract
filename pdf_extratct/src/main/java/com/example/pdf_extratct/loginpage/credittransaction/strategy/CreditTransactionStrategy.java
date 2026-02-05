package com.example.pdf_extratct.loginpage.credittransaction.strategy;


import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;

public interface CreditTransactionStrategy {
    TransactionType getTransactionType();

    CreditTransactionData createTransaction(UserEntity user, Integer amount, String description, String relatedId);

    boolean validateBalance(UserEntity user, Integer amount);
}
