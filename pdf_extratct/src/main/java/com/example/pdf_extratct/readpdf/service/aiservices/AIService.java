package com.example.pdf_extratct.readpdf.service.aiservices;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ExtractionHeaders;
import com.example.pdf_extratct.readpdf.service.database.SystemReadExtractTextService;
import com.example.pdf_extratct.uploadfiles.storage.service.FileSystemStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.Resource;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final VertexAiGeminiChatModel chatModel;
    private  final FileSystemStorageService storageService;

    public AIService(VertexAiGeminiChatModel chatModel, FileSystemStorageService storageService) {
        this.chatModel = chatModel;
        this.storageService = storageService;
    }


    public String exctract(ExtractionHeaders headers, String fullText) {

        if (headers == null || fullText == null) {
            log.error("headers or fullText is null");
        }

        if (fullText.trim().length() < 200) {
            log.info("Usando OCR automático");
            fullText = extractFilesOCR();  // Chama aqui!
        }

        ChatResponse response = chatModel.call(promptLLM(headers, fullText));
        String excelJson = response.getResult().getOutput().getText();

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

    private String extractFilesOCR(){
        List<Path> paths = storageService.loadAll().toList();
        List<Media> mediaList = new ArrayList<>();

        for (Path path : paths) {
            Resource resource = storageService.loadAsResource(path.getFileName().toString());
            mediaList.add(new Media(MimeType.valueOf("application/pdf"), resource));
        }

        SystemMessage systemMessage = new SystemMessage(
                "Você é um especialista em OCR. Extraia TODO o texto visível dos arquivos PDF enviados, sem resumir.");

        UserMessage userMessage = UserMessage.builder()
                .text("Leia os PDFs anexados e transcreva todo o texto.")
                .media(mediaList.toArray(new Media[0]))
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }
}
