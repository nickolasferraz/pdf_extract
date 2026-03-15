package com.example.pdf_extratct.Payment.factory;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.example.pdf_extratct.Payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    // Mapa: PaymentType → Strategy correspondente
    private final Map<PaymentType, PaymentStrategy> strategies;

    // O Spring injeta TODAS as implementações de PaymentStrategy automaticamente
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getType,   // chave: PIX, CREDIT_CARD, CHECKOUT_PRO
                        Function.identity()          // valor: a própria strategy
                ));
    }

    public PaymentStrategy getStrategy(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Tipo de pagamento não suportado: " + type);
        }
        return strategy;
    }
}

