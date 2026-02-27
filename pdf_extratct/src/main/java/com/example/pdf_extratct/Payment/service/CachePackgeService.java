package com.example.pdf_extratct.Payment.service;


import com.example.pdf_extratct.Payment.CreditPackgesRepository;
import com.example.pdf_extratct.Payment.dto.CreditPackgesRequestDTO;
import com.example.pdf_extratct.Payment.models.entity.CreditPackagesEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // Importar Collectors


@Service
public class CachePackgeService {

    private final CreditPackgesRepository creditPackgesRepository;

    public CachePackgeService(CreditPackgesRepository creditPackgesRepository) {
        this.creditPackgesRepository = creditPackgesRepository;
    }


    @Cacheable(value = "CREDIT_PACKAGES_CACHE", key = "#packageId") // Chave de cache por ID
    public CreditPackgesRequestDTO getPackageById(int packageId){ // Renomeado e corrigido
        CreditPackagesEntity  creditPackage = creditPackgesRepository.findByPackageId(packageId);

        return new CreditPackgesRequestDTO(creditPackage.getPackageId(),
                                            creditPackage.getName(),
                                            creditPackage.getCredits(),
                                            creditPackage.getPrice_cents(),
                                            creditPackage.getMoeda()
        );
    }

    @Cacheable(value = "ALL_CREDIT_PACKAGES_CACHE") // Cache para a lista completa
    public List<CreditPackgesRequestDTO> getAllPackages() {
        List<CreditPackagesEntity> allPackages = creditPackgesRepository.findAll();

        // Converte a lista de entidades para uma lista de DTOs
        return allPackages.stream()
                .map(creditPackage -> new CreditPackgesRequestDTO(
                        creditPackage.getPackageId(),
                        creditPackage.getName(),
                        creditPackage.getCredits(),
                        creditPackage.getPrice_cents(),
                        creditPackage.getMoeda()
                ))
                .collect(Collectors.toList());
    }
}
