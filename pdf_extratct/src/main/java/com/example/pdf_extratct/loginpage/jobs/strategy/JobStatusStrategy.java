package com.example.pdf_extratct.loginpage.jobs.strategy;

import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;

public interface JobStatusStrategy {
    JobStatus getTargetStatus();
    void execute(ProcessingJobEntity job, JobContext context);
}
