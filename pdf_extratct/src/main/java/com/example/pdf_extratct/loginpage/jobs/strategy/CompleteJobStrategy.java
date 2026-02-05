package com.example.pdf_extratct.loginpage.jobs.strategy;

import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Instant;


@Component
public class CompleteJobStrategy implements JobStatusStrategy {

    @Override
    public JobStatus getTargetStatus() {
        return JobStatus.COMPLETED;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        if (job.getStatus() != JobStatus.PROCESSING) {
            throw new RuntimeException("Job não está PROCESSING: " + job.getStatus());
        }
        job.setStatus(JobStatus.COMPLETED);
        job.setPagesProcessed(context.pagesProcessed());
        job.setCreditsUsed(context.creditsUsed());
        job.setCompletedAt(Timestamp.from(Instant.now()));
    }
}