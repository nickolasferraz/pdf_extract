package com.example.pdf_extratct.loginpage.jobs.creation;

import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobRepository;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobService;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedJobCreationStrategy implements JobCreationStrategy {

    private final ProcessingJobRepository jobRepository;
    private final ProcessingJobService processingJobService;

    @Override
    public JobCreationType getType() {
        return JobCreationType.AUTHENTICATED;
    }

    @Override
    public ProcessingJobEntity create(JobCreationContext context) {
        ProcessingJobEntity job = ProcessingJobEntity
                .createPending(context.user(), context.fileName(), context.fileSize(), context.estimatedCredits());
        job = jobRepository.save(job);

        if (!hasSufficientCredits(context.user(), context.estimatedCredits())) {
            return processingJobService.cancelJob(job.getJobId(), "Créditos insuficientes para processamento");
        }

        return job;
    }

    private boolean hasSufficientCredits(UserEntity user, Integer required) {
        return user.getCreditBalance() >= required;
    }
}

