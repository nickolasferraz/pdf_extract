package com.example.pdf_extratct.loginpage.credittransaction.querys;

import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;

public interface CreditCommandService {
    void execute(TransactionType type, UserEntity user, Integer amount, String description, String relatedId);
    void assignPurchaseCredits(String userId, int packageId, String paymentId);
}
