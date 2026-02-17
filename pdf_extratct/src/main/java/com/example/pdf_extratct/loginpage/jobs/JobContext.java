package com.example.pdf_extratct.loginpage.jobs;

public record JobContext(
        Integer pagesProcessed,
        Integer creditsUsed,
        String errorMessage,
        String reason
) {
    public static JobContext empty() {
        return new JobContext(null, null, null, null);
    }

    public static JobContext forComplete(Integer pages, Integer credits) {
        return new JobContext(pages, credits, null, null);
    }

    public static JobContext forFail(String error) {
        return new JobContext(null, null, error, null);
    }

    public static JobContext forRefund(String reason) {
        return new JobContext(null, null, null, reason);
    }

    public static JobContext forCancel(String reason){
        return new JobContext(null, null, null, reason);
    }
}
