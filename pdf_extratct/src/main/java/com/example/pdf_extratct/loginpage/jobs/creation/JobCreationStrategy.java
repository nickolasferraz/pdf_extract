package com.example.pdf_extratct.loginpage.jobs.creation;

import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;

public interface JobCreationStrategy {
    JobCreationType getType();
    ProcessingJobEntity create(JobCreationContext context);
}
