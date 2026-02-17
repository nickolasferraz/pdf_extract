package com.example.pdf_extratct.loginpage.jobs;

import com.example.pdf_extratct.loginpage.jobs.Dtos.*;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final ProcessingJobRepository jobRepository;

    public Page<JobResponse> getUserJobs(UserEntity user, Pageable pageable) {
        Page<ProcessingJobEntity> jobs = jobRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return jobs.map(this::mapToResponse);
    }

    public Page<JobResponse> getUserJobsByStatus(
            UserEntity user,
            JobStatus status,
            Pageable pageable) {
        Page<ProcessingJobEntity> jobs = jobRepository
                .findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
        return jobs.map(this::mapToResponse);
    }

    public JobResponse getJobById(UserEntity user, String jobId) {
        ProcessingJobEntity job = jobRepository.findByJobIdAndUser(jobId, user) // CORRIGIDO: findByIdAndUser para findByJobIdAndUser
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToResponse(job);
    }

    public JobStatsResponse getUserStats(UserEntity user) {
        Long total = jobRepository.countByUser(user);
        Long completed = jobRepository.countByUserAndStatus(user, JobStatus.COMPLETED);
        Long failed = jobRepository.countByUserAndStatus(user, JobStatus.FAILED);
        Long pending = jobRepository.countByUserAndStatus(user, JobStatus.PENDING);
        Long canceled=jobRepository.countByUserAndStatus(user,JobStatus.CANCELED);
        Long refunded=jobRepository.countByUserAndStatus(user,JobStatus.REFUNDED);
        Integer totalCredits = jobRepository.sumCreditsUsedByUser(user);

        return new JobStatsResponse(
                total,
                completed,
                failed,
                pending,
                canceled,
                refunded,
                totalCredits != null ? totalCredits : 0
        );
    }

    private JobResponse mapToResponse(ProcessingJobEntity job) {
        return new JobResponse(
                job.getJobId(),
                job.getFileName(),
                job.getStatus(),
                job.getFileSizeBytes(),
                job.getCreditsUsed(),
                job.getCreditsEstimated(),
                job.getPagesProcessed(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt()
        );
    }

}
