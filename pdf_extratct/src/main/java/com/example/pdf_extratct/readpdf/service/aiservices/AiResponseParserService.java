package com.example.pdf_extratct.readpdf.service.aiservices;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class AiResponseParserService {

    private final ObjectMapper objectMapper;

    public AiResponseParserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Map<String, String>> parseRows(String excelJson) {
        try {
            return objectMapper.readValue(
                    excelJson,
                    new TypeReference<List<Map<String, String>>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter resposta da IA", e);
        }
    }
}
