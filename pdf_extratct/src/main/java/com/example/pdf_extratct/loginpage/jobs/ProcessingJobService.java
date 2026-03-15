package com.example.pdf_extratct.loginpage.jobs;

import com.example.pdf_extratct.loginpage.jobs.factory.JobStatusStrategyFactory;
import com.example.pdf_extratct.loginpage.jobs.strategy.JobStatusStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingJobService {

    private final ProcessingJobRepository jobRepository;
    private final JobStatusStrategyFactory strategyFactory;

    // ===============================
    // 🔄 TRANSIÇÕES DE STATUS
    // ===============================

    @Transactional
    public ProcessingJobEntity startProcessing(String jobId) {
        return executeStrategy(jobId, JobStatus.PROCESSING, JobContext.empty());
    }

    @Transactional
    public ProcessingJobEntity completeJob(String jobId, Integer pages, Integer credits) {
        return executeStrategy(jobId, JobStatus.COMPLETED, JobContext.forComplete(pages, credits));
    }

    @Transactional
    public ProcessingJobEntity failJob(String jobId, String error) {
        return executeStrategy(jobId, JobStatus.FAILED, JobContext.forFail(error));
    }

    @Transactional
    public ProcessingJobEntity refundJob(String jobId, String reason) {
        return executeStrategy(jobId, JobStatus.REFUNDED, JobContext.forRefund(reason));
    }

    @Transactional
    public ProcessingJobEntity cancelJob(String jobId, String reason) {
        return executeStrategy(jobId, JobStatus.CANCELED, JobContext.forCancel(reason));
    }


    public void completeAnonymousJob(String jobId, int pagesProcessed, int estimatedCredits) {
        ProcessingJobEntity job = getJobById(jobId);
        job.setStatus(JobStatus.COMPLETED);
        job.setPagesProcessed(pagesProcessed);
        jobRepository.save(job);
    }

    // ===============================
    // 🔧 MÉTODOS INTERNOS
    // ===============================

    public ProcessingJobEntity getJobById(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));
    }

    private ProcessingJobEntity executeStrategy(String jobId, JobStatus targetStatus, JobContext context) {
        ProcessingJobEntity job = getJobById(jobId);
        JobStatusStrategy strategy = strategyFactory.getStrategy(targetStatus);
        strategy.execute(job, context);
        return jobRepository.save(job);
    }

    private boolean hasSufficientCredits(UserEntity user, Integer required) {
        return user.getCreditBalance() >= required;
    }
}
