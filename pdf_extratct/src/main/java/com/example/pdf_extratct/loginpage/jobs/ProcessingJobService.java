package com.example.pdf_extratct.loginpage.jobs;

import com.example.pdf_extratct.loginpage.credittransaction.CreditService;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.jobs.strategy.JobStatusStrategy;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessingJobService {

    private final ProcessingJobRepository jobRepository;
    private final CreditService creditService;
    private final Map<JobStatus, JobStatusStrategy> strategies;

    public ProcessingJobService(
            ProcessingJobRepository jobRepository,
            CreditService creditService,
            List<JobStatusStrategy> strategyList
    ) {
        this.jobRepository = jobRepository;
        this.creditService = creditService;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        JobStatusStrategy::getTargetStatus,
                        Function.identity()
                ));
    }

    private boolean hasSufficientCredits(UserEntity user, Integer required) {
        return user.getCreditBalance() >= required;
    }

    @Transactional
    public ProcessingJobEntity createJob(
            UserEntity user,
            String fileName,
            Long fileSize,
            Integer estimatedCredits
    ) {
        ProcessingJobEntity job = ProcessingJobEntity
                .createPending(user, fileName, fileSize, estimatedCredits);

        if (!hasSufficientCredits(user, estimatedCredits)) {
            return executeStrategy(
                    job.getJobId(),
                    JobStatus.CANCELED,
                    JobContext.forCancel("Créditos insuficientes para processamento"));
        }


        return jobRepository.save(job);
    }



    @Transactional
    public ProcessingJobEntity startProcessing(String jobId) {
        return executeStrategy(jobId, JobStatus.PROCESSING, JobContext.empty());
    }

    @Transactional
    public ProcessingJobEntity completeJob(String jobId, Integer pages, Integer credits) {
        ProcessingJobEntity job = executeStrategy(
                jobId,
                JobStatus.COMPLETED,
                JobContext.forComplete(pages, credits)
        );
        debitJobCredits(job, credits);
        return job;
    }

    @Transactional
    public ProcessingJobEntity failJob(String jobId, String error) {
        return executeStrategy(jobId, JobStatus.FAILED, JobContext.forFail(error));
    }

    @Transactional
    public ProcessingJobEntity refundJob(String jobId, String reason) {
        ProcessingJobEntity job = getJobById(jobId);
        validateRefund(job);
        job.setStatus(JobStatus.REFUNDED);
        jobRepository.save(job);
        refundJobCredits(job, reason);
        return job;
    }

    // Consultas
    public ProcessingJobEntity getJobById(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));
    }

    public List<ProcessingJobEntity> getUserJobs(UserEntity user) {
        return jobRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ProcessingJobEntity> getUserJobsByStatus(UserEntity user, JobStatus status) {
        return jobRepository.findByUserAndStatus(user, status);
    }

    // Métodos privados auxiliares
    private ProcessingJobEntity executeStrategy(String jobId, JobStatus targetStatus, JobContext context) {
        ProcessingJobEntity job = getJobById(jobId);
        JobStatusStrategy strategy = strategies.get(targetStatus);
        if (strategy == null) {
            throw new RuntimeException("Strategy não encontrada: " + targetStatus);
        }
        strategy.execute(job, context);
        return jobRepository.save(job);
    }

    private Boolean validateCredits(UserEntity user, Integer required) {
        if (user.getCreditBalance() < required) {
            throw new RuntimeException(
                    "Créditos insuficientes. Você tem: " + user.getCreditBalance() +
                            ", necessário: " + required
            );
        }
        return true;
    }

    private void validateRefund(ProcessingJobEntity job) {
        if (job.getCreditsUsed() == null || job.getCreditsUsed() == 0) {
            throw new RuntimeException("Job não tem créditos para reembolsar");
        }
    }

    private void debitJobCredits(ProcessingJobEntity job, Integer credits) {
        creditService.debitCredits(
                job.getUser(),
                credits,
                job.getJobId(),
                "Processamento: " + job.getFileName() + " (" + job.getPagesProcessed() + " páginas)"
        );
    }

    private void refundJobCredits(ProcessingJobEntity job, String reason) {
        creditService.addCredits(
                job.getUser(),
                job.getCreditsUsed(),
                TransactionType.REFUND,
                "Reembolso: " + job.getFileName() + " - " + reason
        );
    }


    @Transactional
    public ProcessingJobEntity createAnonymousJob(String fileName,
                                                  Long fileSize,
                                                  Integer estimatedCredits,
                                                  String clientIp) {
        if (estimatedCredits == null) {
            estimatedCredits = 1; // default mínimo
        }
        if (fileName == null) {
            throw new IllegalArgumentException("fileName é obrigatório");
        }

        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setGuestId(GuestIdUtil.toGuestId(clientIp));
        job.setJobId(UUID.randomUUID().toString());
        job.setFileName(fileName);
        job.setFileSizeBytes(fileSize);
        job.setCreditsEstimated(estimatedCredits);
        // definir credits_used para 0 inicialmente (ou igual a estimated se quiser)
        job.setCreditsUsed(0);
        job.setStatus(JobStatus.PENDING);
        job.setAnonymous(true);
        job.setClientIp(clientIp);

            ProcessingJobEntity saved = jobRepository.save(job);

            if (saved == null) {
                throw new IllegalStateException("Repo retornou null ao salvar job");
            }

            return saved;

    }








    public void completeAnonymousJob(String jobId, int pagesProcessed, int estimatedCredits) {
        ProcessingJobEntity job =findJob(jobId);
        job.setStatus(JobStatus.COMPLETED);
        job.setPagesProcessed(pagesProcessed);
        // NÃO debitar créditos do usuário porque é anônimo
        jobRepository.save(job);
    }

    private ProcessingJobEntity findJob(String jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }
}
