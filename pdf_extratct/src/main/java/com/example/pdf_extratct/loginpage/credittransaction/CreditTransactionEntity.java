package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "credit_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Integer balanceBefore;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false)
    private String description;

    private String relatedJobId;
    private String relatedPaymentId;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt = Timestamp.from(Instant.now());
}
