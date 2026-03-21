package com.example.pdf_extratct.loginpage.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findByUserId(String userId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}