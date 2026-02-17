package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.credittransaction.Dtos.AddCreditsRequest;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.CreditBalanceResponse;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.TransactionResponse;
import com.example.pdf_extratct.loginpage.credittransaction.strategy.CreditTransactionStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.logging.ApiLogContext; // Importar ApiLogContext
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ===============================
    // 🔒 MÉTODO BASE SEGURO
    // ===============================
    @Transactional
    public CreditTransactionEntity executeTransaction(
            CreditTransactionStrategy strategy,
            UserEntity user,
            Integer amount,
            String description,
            String relatedId
    ) {

        UserEntity lockedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!strategy.validateBalance(lockedUser, amount)) {
            throw new RuntimeException("Saldo insuficiente");
        }

        CreditTransactionData transactionData =
                strategy.createTransaction(lockedUser, amount, description, relatedId);

        lockedUser.setCreditBalance(transactionData.balanceAfter());
        userRepository.save(lockedUser);

        return transactionRepository.save(transactionData.toEntity());
    }

    // ===============================
    // ➕ ADICIONAR CRÉDITOS
    // ===============================
    @Transactional
    public CreditTransactionEntity addCredits(
            UserEntity user,
            Integer amount,
            TransactionType type,
            String description
    ) {

        UserEntity lockedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        CreditTransactionData data =
                CreditTransactionData.forCredit(lockedUser, amount, type, description);

        lockedUser.setCreditBalance(data.balanceAfter());
        userRepository.save(lockedUser);

        return transactionRepository.save(data.toEntity());
    }

    // ===============================
    // ➖ DEBITAR CRÉDITOS
    // ===============================
    @Transactional
    public CreditTransactionEntity debitCredits(
            UserEntity user,
            Integer amount,
            String jobId,
            String description
    ) {

        UserEntity lockedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (lockedUser.getCreditBalance() < amount) {
            throw new RuntimeException("Saldo insuficiente");
        }

        CreditTransactionData data =
                CreditTransactionData.forDebit(lockedUser, amount, jobId, description);

        lockedUser.setCreditBalance(data.balanceAfter());
        userRepository.save(lockedUser);

        ApiLogContext.setCreditsDeducted(amount);

        return transactionRepository.save(data.toEntity());
    }

    // ===============================
    // 💰 ADD VIA REQUEST
    // ===============================
    @Transactional
    public CreditBalanceResponse addCredits(
            UserEntity user,
            AddCreditsRequest request
    ) {

        UserEntity lockedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        CreditTransactionData data = CreditTransactionData.forCredit(
                lockedUser,
                request.amount(),
                request.type(),
                request.description()
        );

        lockedUser.setCreditBalance(data.balanceAfter());
        userRepository.save(lockedUser);
        transactionRepository.save(data.toEntity());

        return new CreditBalanceResponse(
                lockedUser.getUserId(),
                data.balanceAfter(),
                lockedUser.getEmail()
        );
    }

    // ===============================
    // 📊 CONSULTAR SALDO
    // ===============================
    public CreditBalanceResponse getBalance(UserEntity user) {

        Integer balance = user.getCreditBalance() != 0
                ? user.getCreditBalance()
                : 0;

        return new CreditBalanceResponse(
                user.getUserId(),
                balance,
                user.getEmail()
        );
    }

    // ===============================
    // 📜 HISTÓRICO
    // ===============================
    public Page<TransactionResponse> getTransactionHistory(
            UserEntity user,
            Pageable pageable
    ) {

        Page<CreditTransactionEntity> transactions =
                transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return transactions.map(transaction ->
                new TransactionResponse(
                        transaction.getTransactionId(),
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