package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.CreditPackagesResponseDTO;
import com.example.pdf_extratct.Payment.service.CachePackgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/credit-packages")
public class CreditPackagesController {

    private final CachePackgeService cachePackgeService;
    private final ObjectMapper objectMapper;

    public CreditPackagesController(CachePackgeService cachePackgeService, ObjectMapper objectMapper) {
        this.cachePackgeService = cachePackgeService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<CreditPackagesResponseDTO>> getAllPackages() {
        // O cache retorna uma List<Object>, onde cada objeto é um LinkedHashMap
        List<?> packagesFromCache = cachePackgeService.getAllPackages();

        // Converte manualmente cada LinkedHashMap para o DTO de resposta
        List<CreditPackagesResponseDTO> packages = packagesFromCache.stream()
                .map(obj -> objectMapper.convertValue(obj, CreditPackagesResponseDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<CreditPackagesResponseDTO> getPackageById(@PathVariable int packageId) {
        // O cache retorna um Object (LinkedHashMap)
        Object packageFromCache = cachePackgeService.getPackageById(packageId);

        // Converte manualmente o LinkedHashMap para o DTO de resposta
        CreditPackagesResponseDTO creditPackage = objectMapper.convertValue(packageFromCache, CreditPackagesResponseDTO.class);

        return ResponseEntity.ok(creditPackage);
    }
}
