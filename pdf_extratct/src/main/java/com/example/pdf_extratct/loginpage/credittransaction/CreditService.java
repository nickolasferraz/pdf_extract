package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.credittransaction.Dtos.AddCreditsRequest;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.CreditBalanceResponse;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.TransactionResponse;
import com.example.pdf_extratct.loginpage.credittransaction.strategy.CreditTransactionStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
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

    @Transactional
    public CreditTransactionEntity executeTransaction(
            CreditTransactionStrategy strategy,
            UserEntity user,
            Integer amount,
            String description,
            String relatedId
    ) {
        // Validar saldo
        if (!strategy.validateBalance(user, amount)) {
            throw new RuntimeException("Saldo insuficiente");
        }

        // Criar dados da transação usando strategy
        CreditTransactionData transactionData = strategy.createTransaction(user, amount, description, relatedId);

        // Atualizar saldo do usuário
        user.setCreditBalance(transactionData.balanceAfter());
        userRepository.save(user);

        // Salvar transação
        return transactionRepository.save(transactionData.toEntity());
    }

    // Métodos helper para facilitar o uso
    @Transactional
    public CreditTransactionEntity addCredits(
            UserEntity user,
            Integer amount,
            TransactionType type,
            String description
    ) {
        CreditTransactionData data = CreditTransactionData.forCredit(user, amount, type, description);
        user.setCreditBalance(data.balanceAfter());
        userRepository.save(user);
        return transactionRepository.save(data.toEntity());
    }

    @Transactional
    public CreditTransactionEntity debitCredits(
            UserEntity user,
            Integer amount,
            String jobId,
            String description
    ) {
        if (user.getCreditBalance() < amount) {
            throw new RuntimeException("Saldo insuficiente");
        }

        CreditTransactionData data = CreditTransactionData.forDebit(user, amount, jobId, description);
        user.setCreditBalance(data.balanceAfter());
        userRepository.save(user);
        return transactionRepository.save(data.toEntity());
    }

    @Transactional
    public CreditBalanceResponse addCredits(UserEntity user, AddCreditsRequest request) {
        CreditTransactionData data = CreditTransactionData.forCredit(
                user,
                request.amount(),
                request.type(),
                request.description()
        );

        user.setCreditBalance(data.balanceAfter());
        userRepository.save(user);
        transactionRepository.save(data.toEntity());

        return new CreditBalanceResponse(
                user.getUserId(),
                data.balanceAfter(),
                user.getEmail()
        );
    }

    public CreditBalanceResponse getBalance(UserEntity user) {
        Integer balance = user.getCreditBalance() != null ? user.getCreditBalance() : 0;
        return new CreditBalanceResponse(
                user.getUserId(),
                balance,
                user.getEmail()
        );
    }

    // ✅ ADICIONAR ESTE MÉTODO
    public Page<TransactionResponse> getTransactionHistory(UserEntity user, Pageable pageable) {
        Page<CreditTransactionEntity> transactions = transactionRepository
                .findByUserOrderByCreatedAtDesc(user, pageable);

        return transactions.map(transaction -> new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getRelatedJobId(),
                transaction.getCreatedAt()
        ));
    }
}
