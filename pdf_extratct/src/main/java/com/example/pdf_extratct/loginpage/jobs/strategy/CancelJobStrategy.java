package com.example.pdf_extratct.loginpage.jobs.strategy;

import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;

import java.sql.Timestamp;
import java.time.Instant;

public class CancelJobStrategy implements JobStatusStrategy{

    @Override
    public JobStatus getTargetStatus() {
        return null;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        job.setStatus(JobStatus.CANCELED);
        job.setPagesProcessed(context.pagesProcessed());
        job.setCreditsUsed(context.creditsUsed());
        job.setCompletedAt(Timestamp.from(Instant.now()));

    }
}
