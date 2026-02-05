package com.example.pdf_extratct.loginpage.auth;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccountEntity, String> {

    Optional<AuthAccountEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);

    Optional<AuthAccountEntity> findByUserAndProvider(UserEntity user, AuthProvider provider);
}
