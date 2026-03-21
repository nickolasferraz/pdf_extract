package com.example.pdf_extratct.loginpage.jobs;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJobEntity, String> {
    Page<ProcessingJobEntity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
    List<ProcessingJobEntity> findByUserAndStatus(UserEntity user, JobStatus status);

    Page<ProcessingJobEntity> findByUserAndStatusOrderByCreatedAtDesc(
            UserEntity user,
            JobStatus status,
            Pageable pageable
    );



    List<ProcessingJobEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    Long countByUser(UserEntity user);

    Long countByUserAndStatus(UserEntity user, JobStatus status);

    @Query("SELECT SUM(j.creditsUsed) FROM ProcessingJobEntity j WHERE j.user = :user")
    Integer sumCreditsUsedByUser(UserEntity user);

    Optional<ProcessingJobEntity>findByJobIdAndUser(String jobId, UserEntity user); // CORRIGIDO: findByIdAndUser para findByJobIdAndUser
}
