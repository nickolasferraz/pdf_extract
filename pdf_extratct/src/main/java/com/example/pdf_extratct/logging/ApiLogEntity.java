package com.example.pdf_extratct.logging;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "api_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogEntity {

    @Id
    @Column(name = "log_id")
    private String logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // user_id pode ser nulo se a requisição não for autenticada
    private UserEntity user;

    @Column(nullable = false)
    private String endpoint;

    @Column(length = 10) // GET, POST, PUT, DELETE
    private String method;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Usar Long para tempo

    @Column(name = "credits_deducted")
    private Integer creditsDeducted = 0; // Default para 0

    @Column(name = "ip_address") // Removido columnDefinition = "inet"
    private String ipAddress; // Tipo 'inet' no DB, mas String no Java é comum

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @PrePersist
    public void generateId() {
        if (this.logId == null) {
            this.logId = UUID.randomUUID().toString();
        }
    }
}
