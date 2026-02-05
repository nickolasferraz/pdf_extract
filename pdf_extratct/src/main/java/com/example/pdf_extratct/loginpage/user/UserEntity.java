package com.example.pdf_extratct.loginpage.user;


import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.credittransaction.CreditTransactionEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.security.Timestamp;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String password; // Nullable para OAuth-only

    @Column(name = "email_validado")
    private Boolean emailValidado = false;

    @Column(name = "credit_balance")
    private Integer creditBalance = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuthAccountEntity> authAccountEntities;

    @OneToMany(mappedBy = "user" ,fetch = FetchType.LAZY)
    private List<CreditTransactionEntity> transactions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ProcessingJobEntity> jobs;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;


}