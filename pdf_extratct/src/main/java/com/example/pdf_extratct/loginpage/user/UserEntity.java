package com.example.pdf_extratct.loginpage.user;

import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;  // ← ADICIONE ESTE IMPORT
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(name = "email_validado", nullable = false)
    private Boolean emailValidado = false;

    @Column(name = "credit_balance", nullable = false)
    private int creditBalance = 0;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AuthAccountEntity> authAccountEntities;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CreditTransactionEntity> transactions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProcessingJobEntity> jobs;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}