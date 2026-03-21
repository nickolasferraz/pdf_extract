package com.example.pdf_extratct.readpdf.service.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.example.pdf_extratct.readpdf.service.pdfservices.ServiceReadExtractText;
import org.springframework.core.io.FileSystemResource;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ReadExtractProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.stereotype.Service;

@Service
public class SystemReadExtractTextService implements ServiceReadExtractText {

    private final Path path;

    public SystemReadExtractTextService(ReadExtractProperties properties) {
        if (properties.getLocation() == null || properties.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Não há arquivos no path");
        }
        this.path = Paths.get(properties.getLocation());
    }

    public Path getPath() {
        return this.path;
    }


    @Override
    public Stream<Path> loadAllfiles() {

        if(!Files.exists(this.path)  || !Files.isDirectory(this.path)) {
            return Stream.empty();
        }

        try {
            return Files.walk(this.path,1)
                    .filter(p-> !p.equals(this.path))
                    .filter(Files::isRegularFile)
                    .map(this.path::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler arquivos armazenados", e);
        }
    }

    @Override
    public Stream<Document> extracttext() {
        return loadAllfiles()
                .filter(path1 -> path1.toString().toLowerCase().endsWith(".pdf"))
                .map(relativePath -> {
                    Path absolutePath = this.path.resolve(relativePath);
                    PagePdfDocumentReader reader = new PagePdfDocumentReader(new FileSystemResource(absolutePath));
                    return reader.read().stream();
                })
                .flatMap(s -> s);
    }



}
