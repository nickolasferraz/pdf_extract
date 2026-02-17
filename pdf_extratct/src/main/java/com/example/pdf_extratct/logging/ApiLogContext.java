package com.example.pdf_extratct.logging;

public class ApiLogContext {
    private static final ThreadLocal<Integer> creditsDeducted = new ThreadLocal<>();

    public static void setCreditsDeducted(Integer credits) {
        creditsDeducted.set(credits);
    }

    public static Integer getCreditsDeducted() {
        return creditsDeducted.get();
    }

    public static void clear() {
        creditsDeducted.remove();
    }
}
