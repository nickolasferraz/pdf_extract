package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransactionEntity, Long> {
    Page<CreditTransactionEntity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
    
    // Suporte para busca pelo UUID do usuário usando a propriedade aninhada
    Page<CreditTransactionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
