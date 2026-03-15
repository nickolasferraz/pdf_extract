package com.example.pdf_extratct.readpdf.service.aiservices.strategy;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextAiExtractionStrategy implements AiExtractionStrategy {

    private final VertexAiGeminiChatModel chatModel;

    @Override
    public boolean supports(int textLength) {
        return textLength >= 200;
    }

    @Override
    public String extract(ExtractionHeaders headers, AiExtractionContext context) {
        log.info("Usando TextAiExtractionStrategy (Extração Normal)");
        
        ChatResponse response = chatModel.call(promptLLM(headers, context.fullText()));
        String excelJson = response.getResult().getOutput().getText();

        if (excelJson == null || excelJson.trim().isEmpty()) {
            log.error("Resposta da IA vazia ou nula para extração de documento.");
            throw new RuntimeException("A IA não retornou dados. Verifique a conexão ou o prompt.");
        }

        return excelJson;
    }

    private Prompt promptLLM(ExtractionHeaders headers, String fullText) {
        SystemMessage systemMessage =
                new SystemMessage("""
                        Extraia APENAS estes campos: %s
                        
                        Responda SOMENTE com JSON válido.
                        Formato:
                        [
                          {"nome":"...", "cpf":"..."}
                        ]
                        
                        NÃO use markdown.
                        NÃO use ```json.
                        NÃO escreva explicações.
                        """
                        .formatted(headers));

        UserMessage userMessage = new UserMessage(fullText);

        return new Prompt(List.of(systemMessage, userMessage));
    }
}
