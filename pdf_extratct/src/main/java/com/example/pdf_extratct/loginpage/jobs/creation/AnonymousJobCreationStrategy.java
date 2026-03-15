package com.example.pdf_extratct.loginpage.jobs.creation;

import com.example.pdf_extratct.loginpage.jobs.GuestIdUtil;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnonymousJobCreationStrategy implements JobCreationStrategy {

    private final ProcessingJobRepository jobRepository;

    @Override
    public JobCreationType getType() {
        return JobCreationType.ANONYMOUS;
    }

    @Override
    public ProcessingJobEntity create(JobCreationContext context) {
        if (context.fileName() == null) {
            throw new IllegalArgumentException("fileName é obrigatório");
        }
        
        Integer estimatedCredits = context.estimatedCredits();
        if (estimatedCredits == null) {
            estimatedCredits = 1;
        }

        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setGuestId(GuestIdUtil.toGuestId(context.clientIp()));
        job.setJobId(UUID.randomUUID().toString());
        job.setFileName(context.fileName());
        job.setFileSizeBytes(context.fileSize());
        job.setCreditsEstimated(estimatedCredits);
        job.setCreditsUsed(0);
        job.setStatus(JobStatus.PENDING);
        job.setAnonymous(true);
        job.setClientIp(context.clientIp());

        return jobRepository.save(job);
    }
}
