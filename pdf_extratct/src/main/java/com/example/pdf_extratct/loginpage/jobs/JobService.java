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
        ProcessingJobEntity job = jobRepository.findByIdAndUser(jobId, user)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToResponse(job);
    }

    public JobStatsResponse getUserStats(UserEntity user) {
        Long total = jobRepository.countByUser(user);
        Long completed = jobRepository.countByUserAndStatus(user, JobStatus.COMPLETED);
        Long failed = jobRepository.countByUserAndStatus(user, JobStatus.FAILED);
        Long pending = jobRepository.countByUserAndStatus(user, JobStatus.PENDING);
        Integer totalCredits = jobRepository.sumCreditsUsedByUser(user);

        return new JobStatsResponse(
                total,
                completed,
                failed,
                pending,
                totalCredits != null ? totalCredits : 0
        );
    }

    @Transactional
    public JobResponse createJob(UserEntity user, String fileName, Long fileSize) {
        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setUser(user);
        job.setFileName(fileName);
        job.setFileSizeBytes(fileSize);
        job.setStatus(JobStatus.PENDING);

        job = jobRepository.save(job);
        return mapToResponse(job);
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
