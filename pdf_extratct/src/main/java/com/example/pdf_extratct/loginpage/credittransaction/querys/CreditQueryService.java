package com.example.pdf_extratct.loginpage.credittransaction.querys;

import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionRepository;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.CreditBalanceResponse;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.TransactionResponse;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditQueryService {

    private final CreditTransactionRepository transactionRepository;

    // 📊 CONSULTAR SALDO
    public CreditBalanceResponse getBalance(UserEntity user) {
        return new CreditBalanceResponse(
                user.getUserId(),
                user.getCreditBalance(),
                user.getEmail()
        );
    }

    // 📜 HISTÓRICO
    public Page<TransactionResponse> getTransactionHistory(UserEntity user, Pageable pageable) {
        Page<CreditTransactionEntity> transactions =
                transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return transactions.map(transaction ->
                new TransactionResponse(
                        String.valueOf(transaction.getTransactionId()),
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getBalanceBefore(),
                        transaction.getBalanceAfter(),
                        transaction.getDescription(),
                        transaction.getRelatedJobId(),
                        transaction.getCreatedAt()
                )
        );
    }
}
