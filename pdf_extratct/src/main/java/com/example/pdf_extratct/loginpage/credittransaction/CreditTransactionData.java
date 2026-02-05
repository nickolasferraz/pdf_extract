package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.user.UserEntity;

public record CreditTransactionData(
        UserEntity user,
        Integer amount,
        TransactionType type,
        Integer balanceBefore,
        Integer balanceAfter,
        String description,
        String relatedJobId
) {
    // Construtor compacto para validação
    public CreditTransactionData {
        if (amount == null || amount == 0) {
            throw new IllegalArgumentException("Amount não pode ser zero");
        }
    }

    // Factory method para crédito (positivo)
    public static CreditTransactionData forCredit(
            UserEntity user,
            Integer amount,
            TransactionType type,
            String description
    ) {
        Integer balanceBefore = user.getCreditBalance();
        Integer balanceAfter = balanceBefore + amount;

        return new CreditTransactionData(
                user, amount, type, balanceBefore, balanceAfter, description, null
        );
    }

    // Factory method para débito (negativo)
    public static CreditTransactionData forDebit(
            UserEntity user,
            Integer amount,
            String jobId,
            String description
    ) {
        Integer balanceBefore = user.getCreditBalance();
        Integer balanceAfter = balanceBefore - amount;

        return new CreditTransactionData(
                user, -amount, TransactionType.USAGE, balanceBefore, balanceAfter, description, jobId
        );
    }

    // Converter para Entity
    public CreditTransactionEntity toEntity() {
        CreditTransactionEntity transaction = new CreditTransactionEntity();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setRelatedJobId(relatedJobId);
        return transaction;
    }
}

