package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_transactions")
public class CreditTransactionEntity {

    @Id
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 255)
    private TransactionType type;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "payment_id", length = 255)
    private String paymentId;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "balance_before")
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "related_job_id", length = 255)
    private String relatedJobId;

    @Column(name = "related_payment_id", length = 255)
    private String relatedPaymentId;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "timestamp without time zone default now()")
    private Timestamp createdAt = Timestamp.from(Instant.now());

    @PrePersist
    protected void onCreate() {
        if (this.transactionId == null) {
            this.transactionId = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Timestamp.from(Instant.now());
        }
    }
}
