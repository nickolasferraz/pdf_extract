package com.example.pdf_extratct.loginpage.credittransaction.querys;

import com.example.pdf_extratct.creditpackges.CreditPackagesEntity;
import com.example.pdf_extratct.creditpackges.CreditPackgesRepository;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionData;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionRepository;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.credittransaction.strategy.CreditTransactionStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditCommandServiceImpl implements CreditCommandService {

    private final UserRepository userRepository;
    private final CreditTransactionRepository transactionRepository;
    private final CreditPackgesRepository creditPackgesRepository;
    private final Map<TransactionType, CreditTransactionStrategy> strategies;

    @Transactional
    @Override
    public void execute(TransactionType type, UserEntity user, Integer amount, String description, String relatedId) {
        CreditTransactionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia não encontrada para o tipo: " + type);
        }

        if (!strategy.validateBalance(user, amount)) {
            throw new IllegalStateException("Saldo insuficiente para a operação.");
        }

        CreditTransactionData transactionData = strategy.createTransaction(user, amount, description, relatedId);
        CreditTransactionEntity transaction = transactionData.toEntity();

        user.setCreditBalance(transactionData.balanceAfter());

        userRepository.save(user);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void assignPurchaseCredits(String userId, int packageId, String paymentId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        CreditPackagesEntity creditPackage = creditPackgesRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        execute(
                TransactionType.PURCHASE,
                user,
                creditPackage.getCredits(),
                "Compra de " + creditPackage.getCredits() + " créditos",
                paymentId
        );
    }
}
