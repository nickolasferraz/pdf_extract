package com.example.pdf_extratct.readpdf.service.aiservices.strategy;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AiExtractionFactory {

    private final List<AiExtractionStrategy> strategies;

    public AiExtractionFactory(List<AiExtractionStrategy> strategies) {
        this.strategies = strategies;
    }

    public AiExtractionStrategy getStrategy(int textLength) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(textLength))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma estratégia de IA suporta a extração deste documento. Tamanho: " + textLength));
    }
}
