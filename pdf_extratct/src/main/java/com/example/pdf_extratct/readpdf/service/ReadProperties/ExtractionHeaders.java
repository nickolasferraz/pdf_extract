package com.example.pdf_extratct.readpdf.service.ReadProperties;

import java.util.List;

public record ExtractionHeaders(List<String> fields) {

    public ExtractionHeaders {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Headers não podem ser vazios");
        }
    }
}
