package com.example.pdf_extratct.readpdf.service.aiservices.strategy;

import org.springframework.core.io.Resource;
import java.util.List;
import lombok.Builder;

@Builder
public record AiExtractionContext(
        String fullText,
        List<Resource> files
) {
}
