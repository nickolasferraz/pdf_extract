package com.example.pdf_extratct.loginpage.controllers;


import com.example.pdf_extratct.loginpage.jobs.Dtos.JobResponse;
import com.example.pdf_extratct.loginpage.jobs.Dtos.JobStatsResponse;
import com.example.pdf_extratct.loginpage.jobs.JobService;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getJobs(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getUserJobs(user, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<JobResponse>> getJobsByStatus(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getUserJobsByStatus(user, status, pageable));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable String jobId) {

        return ResponseEntity.ok(jobService.getJobById(user, jobId));
    }

    @GetMapping("/stats")
    public ResponseEntity<JobStatsResponse> getStats(
            @AuthenticationPrincipal UserEntity user) {

        return ResponseEntity.ok(jobService.getUserStats(user));
    }
}