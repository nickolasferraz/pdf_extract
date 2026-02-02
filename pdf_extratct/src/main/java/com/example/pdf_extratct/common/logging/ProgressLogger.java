package com.example.pdf_extratct.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressLogger {

    private static final Logger log = LoggerFactory.getLogger(ProgressLogger.class);
    private static final int TOTAL_BARS = 20;

    private final long startTime;

    public ProgressLogger() {
        this.startTime = System.currentTimeMillis();
    }

    /* =====================
       MÉTODO GENÉRICO
       ===================== */
    public void step(int percent, String step) {
        logProgress(percent, step);
    }

    /* =====================
       MÉTODO ESPECÍFICO PDF
       ===================== */
    public void pdfPage(int currentPage, int totalPages) {
        int percent = (int) ((currentPage / (double) totalPages) * 30);
        // 0–30% reservado para leitura do PDF

        logProgress(
                percent,
                String.format("Reading PDF (page %d/%d)", currentPage, totalPages)
        );
    }

    /* =====================
       FINALIZAÇÃO
       ===================== */
    public void finish(String message) {
        logProgress(100, message);
        log.info("Process finished in {}s",
                elapsedSeconds());
    }

    /* =====================
       MÉTODO PRIVADO
       ===================== */
    private void logProgress(int percent, String step) {
        int filledBars = percent * TOTAL_BARS / 100;

        String bar = "█".repeat(filledBars)
                + "░".repeat(TOTAL_BARS - filledBars);

        log.info("[{}] {}% - {} ({}s)",
                bar,
                percent,
                step,
                elapsedSeconds());
    }

    private double elapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }
}
