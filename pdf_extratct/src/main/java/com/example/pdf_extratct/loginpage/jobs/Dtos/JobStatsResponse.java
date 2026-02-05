package com.example.pdf_extratct.loginpage.jobs.Dtos;

public record JobStatsResponse(
        Long totalJobs,
        Long completedJobs,
        Long failedJobs,
        Long pendingJobs,
        Integer totalCreditsUsed
) {}
