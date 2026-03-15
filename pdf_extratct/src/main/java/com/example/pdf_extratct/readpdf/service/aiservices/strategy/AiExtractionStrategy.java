package com.example.pdf_extratct.readpdf.service.aiservices.strategy;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;

public interface AiExtractionStrategy {
    
    /**
     * Define se a estratégia suporta a extração baseada no comprimento do texto extraído.
     */
    boolean supports(int textLength);

    /**
     * Executa a extração conversando com a IA.
     */
    String extract(ExtractionHeaders headers, AiExtractionContext context);
}
