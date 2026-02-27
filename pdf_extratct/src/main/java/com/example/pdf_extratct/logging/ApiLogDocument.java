package com.example.pdf_extratct.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "api_logs")  // ← substitui @Entity + @Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogDocument {

    @Id
    private String logId = UUID.randomUUID().toString();

    @Field("user_id")
    private String userId;  // Apenas o ID, sem @ManyToOne (MongoDB não tem JOINs)

    @Field("user_email")
    private String userEmail;  // Desnormalizado para consultas rápidas

    private String endpoint;

    private String method;

    @Field("status_code")
    private Integer statusCode;

    @Field("response_time_ms")
    private Long responseTimeMs;

    @Field("credits_deducted")
    private Integer creditsDeducted = 0;

    @Field("ip_address")
    private String ipAddress;

    @Field("created_at")
    private Instant createdAt = Instant.now();
}