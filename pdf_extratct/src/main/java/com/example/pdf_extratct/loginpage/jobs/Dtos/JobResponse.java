package com.example.pdf_extratct.loginpage.jobs.Dtos;

import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import java.sql.Timestamp;

public record JobResponse(
        String jobId,
        String fileName,
        JobStatus status,
        Long fileSizeBytes,
        Integer creditsUsed,
        Integer creditsEstimated,
        Integer pagesProcessed,
        String errorMessage,
        Timestamp createdAt,
        Timestamp startedAt,
        Timestamp completedAt
) {}
