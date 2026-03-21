package com.example.pdf_extratct.readpdf.service.pdfservices;

import org.springframework.ai.document.Document;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface ServiceReadExtractText {

    Stream<Path>loadAllfiles();

    Stream<Document>extracttext();
}
