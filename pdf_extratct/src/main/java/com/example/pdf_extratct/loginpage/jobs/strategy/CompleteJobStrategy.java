package com.example.pdf_extratct.loginpage.jobs.strategy;

import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.credittransaction.querys.CreditCommandService;
import com.example.pdf_extratct.loginpage.jobs.JobContext;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CompleteJobStrategy implements JobStatusStrategy {

    private final CreditCommandService creditCommandService;

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

        creditCommandService.execute(
                TransactionType.USAGE,
                job.getUser(),
                context.creditsUsed(),
                "Processamento: " + job.getFileName() + " (" + context.pagesProcessed() + " páginas)",
                job.getJobId()
        );
    }
}