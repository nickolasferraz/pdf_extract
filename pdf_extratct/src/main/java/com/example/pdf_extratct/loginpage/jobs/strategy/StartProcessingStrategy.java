package com.example.pdf_extratct.loginpage.jobs.strategy;


import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class StartProcessingStrategy implements JobStatusStrategy {

    @Override
    public JobStatus getTargetStatus() {
        return JobStatus.PROCESSING;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        if (job.getStatus() != JobStatus.PENDING) {
            throw new RuntimeException("Job não está PENDING: " + job.getStatus());
        }
        job.setStatus(JobStatus.PROCESSING);
        job.setStartedAt(Timestamp.from(Instant.now()));
    }
}