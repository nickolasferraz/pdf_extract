package com.example.pdf_extratct.loginpage.jobs;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp; // CORREÇÃO: era java.security.Timestamp
import java.util.UUID;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingJobEntity {

    @Id
    @Column(name = "job_id") // Mapear explicitamente
    private String jobId;

    @Getter
    @Setter
    private Boolean anonymous = false;
    @Getter
    @Setter
    private String clientIp;

    @Getter
    @Setter
    @Column(name = "guest_id", length = 255, nullable = true)
    private String guestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private UserEntity user;

    @Column(name = "credits_used")
    private Integer creditsUsed;

    @Column(name = "credits_estimated")
    private Integer creditsEstimated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "pages_processed")
    private Integer pagesProcessed;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Timestamp startedAt;

    @Column(name = "completed_at")
    private Timestamp completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    public static ProcessingJobEntity createPending(
            UserEntity user,
            String fileName,
            Long fileSize,
            Integer estimatedCredits
    ) {
        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setUser(user);
        job.setFileName(fileName);
        job.setFileSizeBytes(fileSize);
        job.setCreditsEstimated(estimatedCredits);
        job.setCreditsUsed(0);
        job.setStatus(JobStatus.PENDING);
        return job;
    }

    // Gerar UUID antes de persistir
    @PrePersist
    public void generateId() {
        if (this.jobId == null) {
            this.jobId = UUID.randomUUID().toString();
        }
    }
}
