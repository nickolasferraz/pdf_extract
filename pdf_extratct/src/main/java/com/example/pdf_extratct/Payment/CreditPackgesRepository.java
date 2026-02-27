package com.example.pdf_extratct.Payment;

import com.example.pdf_extratct.Payment.models.entity.CreditPackagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditPackgesRepository  extends JpaRepository<CreditPackagesEntity, Integer> {

    @Override
    List<CreditPackagesEntity> findAll();

    CreditPackagesEntity findByPackageId(int packageId);

}
