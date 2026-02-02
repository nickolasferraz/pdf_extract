package com.example.pdf_extratct.readpdf.service.pdfservices;
import org.springframework.ai.document.Document;
import com.example.pdf_extratct.readpdf.service.database.SystemReadExtractTextService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class PdfTextAggregationService {

    private final SystemReadExtractTextService extractTextService;


    public PdfTextAggregationService (SystemReadExtractTextService extractTextService) {
        this.extractTextService = extractTextService;
    }

    public String aggreateFullText(){

        Map<String,List<Document>> groupedByPdf = extractTextAndGroup();


        return buildFulltext(groupedByPdf);

    }

    private Map<String,List<Document>> extractTextAndGroup(){
        return extractTextService.extracttext()
                .collect(Collectors.groupingBy(
                        doc->doc.getMetadata()
                                .getOrDefault("METADATA_FILE_NAME", "sem_nome.pdf")
                                .toString()
                ));
    }

    private String buildFulltext(Map<String,List<Document>> groupedByPdf){
        return groupedByPdf.values().stream()
                .flatMap(List::stream)
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--- NOVO PDF ---\n\n"));
    }




}
