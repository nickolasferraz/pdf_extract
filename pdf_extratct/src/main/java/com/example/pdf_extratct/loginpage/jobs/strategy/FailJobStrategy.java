package com.example.pdf_extratct.loginpage.jobs.strategy;


import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class FailJobStrategy implements JobStatusStrategy {

    @Override
    public JobStatus getTargetStatus() {
        return JobStatus.FAILED;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(context.errorMessage());
        job.setCompletedAt(Timestamp.from(Instant.now()));
    }
}