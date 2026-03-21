package com.example.pdf_extratct.loginpage.jobs.strategy;

import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class CancelJobStrategy implements JobStatusStrategy {

    @Override
    public JobStatus getTargetStatus() {
        return JobStatus.CANCELED;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        job.setStatus(JobStatus.CANCELED);
        job.setErrorMessage(context.reason());
        job.setCompletedAt(Timestamp.from(Instant.now()));
    }
}

