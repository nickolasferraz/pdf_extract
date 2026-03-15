package com.example.pdf_extratct.loginpage.credittransaction.factory;

import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.credittransaction.strategy.CreditTransactionStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreditStrategyFactory {

    private final Map<TransactionType, CreditTransactionStrategy> strategies;

    public CreditStrategyFactory(List<CreditTransactionStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        CreditTransactionStrategy::getTransactionType,
                        Function.identity()
                ));
    }

    public CreditTransactionStrategy getStrategy(TransactionType type) {
        CreditTransactionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia não encontrada para o tipo: " + type);
        }
        return strategy;
    }
}
