package com.example.pdf_extratct.factory;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static UserEntity createUser(String username, String email, String password) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    public static CreditTransactionEntity createCreditTransaction(UserEntity user, int amount, TransactionType type, LocalDateTime createdAt) {
        CreditTransactionEntity transaction = new CreditTransactionEntity();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCreatedAt(Timestamp.valueOf(createdAt));
        return transaction;
    }
}
