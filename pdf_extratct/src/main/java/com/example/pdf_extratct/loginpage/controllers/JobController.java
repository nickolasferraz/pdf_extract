package com.example.pdf_extratct.loginpage.controllers;


import com.example.pdf_extratct.loginpage.jobs.Dtos.JobResponse;
import com.example.pdf_extratct.loginpage.jobs.Dtos.JobStatsResponse;
import com.example.pdf_extratct.loginpage.jobs.JobService;
import com.example.pdf_extratct.loginpage.jobs.JobStatus;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Gerenciamento de Jobs", description = "APIs para listar, buscar e obter estatísticas sobre os jobs de processamento.")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Listar jobs do usuário", description = "Retorna uma lista paginada de jobs associados ao usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista de jobs retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JobResponse.class)))
    public ResponseEntity<Page<JobResponse>> getJobs(
            @AuthenticationPrincipal UserEntity user,
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getUserJobs(user, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar jobs do usuário por status", description = "Retorna uma lista paginada de jobs com um status específico, associados ao usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista de jobs retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JobResponse.class)))
    public ResponseEntity<Page<JobResponse>> getJobsByStatus(
            @AuthenticationPrincipal UserEntity user,
            @Parameter(description = "Status do job (PENDING, PROCESSING, COMPLETED, FAILED)", example = "COMPLETED")
            @PathVariable JobStatus status,
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getUserJobsByStatus(user, status, pageable));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Obter detalhes de um job por ID", description = "Retorna os detalhes de um job específico, se o usuário autenticado tiver acesso a ele.")
    @ApiResponse(responseCode = "200", description = "Detalhes do job retornados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JobResponse.class)))
    @ApiResponse(responseCode = "404", description = "Job não encontrado", content = @Content)
    public ResponseEntity<JobResponse> getJobById(
            @AuthenticationPrincipal UserEntity user,
            @Parameter(description = "ID do job", example = "a1b2c3d4-e5f6-7788-9900-aabbccddeeff")
            @PathVariable String jobId) {

        return ResponseEntity.ok(jobService.getJobById(user, jobId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas dos jobs do usuário", description = "Retorna estatísticas agregadas sobre os jobs do usuário autenticado (total, concluídos, falhados, etc.).")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JobStatsResponse.class)))
    public ResponseEntity<JobStatsResponse> getStats(
            @AuthenticationPrincipal UserEntity user) {

        return ResponseEntity.ok(jobService.getUserStats(user));
    }
}
