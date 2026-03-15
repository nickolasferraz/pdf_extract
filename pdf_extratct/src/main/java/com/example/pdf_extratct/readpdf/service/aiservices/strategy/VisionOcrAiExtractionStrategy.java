package com.example.pdf_extratct.readpdf.service.aiservices.strategy;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisionOcrAiExtractionStrategy implements AiExtractionStrategy {

    private final VertexAiGeminiChatModel chatModel;

    @Override
    public boolean supports(int textLength) {
        return textLength < 200;
    }

    @Override
    public String extract(ExtractionHeaders headers, AiExtractionContext context) {
        log.info("Usando VisionOcrAiExtractionStrategy (Extração Multimodal com OCR)");
        
        List<Resource> files = context.files();
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Para usar OCR, é necessário fornecer os arquivos de imagem/PDF no contexto.");
        }

        List<Media> mediaList = new ArrayList<>();
        for (Resource resource : files) {
            mediaList.add(new Media(MimeType.valueOf("application/pdf"), resource));
        }

        SystemMessage systemMessage = new SystemMessage("""
                Você é um especialista em OCR estruturado.
                Extraia APENAS ou TODO conteúdo listado abaixo: %s
                
                Leia os PDFs anexados e extraia os dados perfeitamente.
                Responda SOMENTE com JSON válido.
                Formato:
                [
                  {"nome":"...", "cpf":"..."}
                ]
                
                NÃO use markdown.
                NÃO use ```json.
                NÃO escreva explicações.
                """.formatted(headers));

        UserMessage userMessage = UserMessage.builder()
                .text("Leia os documentos físicos anexados e transcreva usando as regras.")
                .media(mediaList.toArray(new Media[0]))
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse response = chatModel.call(prompt);
        
        String excelJson = response.getResult().getOutput().getText();

        if (excelJson == null || excelJson.trim().isEmpty()) {
            log.error("Resposta da IA vazia ou nula para OCR.");
            throw new RuntimeException("A IA não retornou dados do OCR. Verifique a conexão ou os arquivos PDF.");
        }

        return excelJson;
    }
}
