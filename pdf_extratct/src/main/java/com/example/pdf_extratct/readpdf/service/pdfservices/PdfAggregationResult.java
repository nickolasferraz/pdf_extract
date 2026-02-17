package com.example.pdf_extratct.readpdf.service.pdfservices;

public class PdfAggregationResult {


    private final String fullText;
    private final int totalPages;

    public PdfAggregationResult(String fullText, int totalPages) {
        this.fullText = fullText;
        this.totalPages = totalPages;
    }


    public String getFullText() {
        return fullText;
    }

    public int getTotalPages() {
        return totalPages;
    }


}
