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
public class RefundJobStrategy implements JobStatusStrategy {

    private final CreditCommandService creditCommandService;

    @Override
    public JobStatus getTargetStatus() {
        return JobStatus.REFUNDED;
    }

    @Override
    public void execute(ProcessingJobEntity job, JobContext context) {
        if (job.getCreditsUsed() == null || job.getCreditsUsed() == 0) {
            throw new RuntimeException("Job não tem créditos para reembolsar");
        }

        job.setStatus(JobStatus.REFUNDED);
        job.setCompletedAt(Timestamp.from(Instant.now()));

        creditCommandService.execute(
                TransactionType.REFUND,
                job.getUser(),
                job.getCreditsUsed(),
                "Reembolso: " + job.getFileName() + " - " + context.reason(),
                job.getJobId()
        );
    }
}
