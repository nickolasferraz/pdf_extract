package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.CreditPackgesRequestDTO;
import com.example.pdf_extratct.Payment.service.CachePackgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-packages")
public class CreditPackagesController {

    private final CachePackgeService cachePackgeService;

    public CreditPackagesController(CachePackgeService cachePackgeService) {
        this.cachePackgeService = cachePackgeService;
    }

    @GetMapping
    public ResponseEntity<List<CreditPackgesRequestDTO>> getAllPackages() {
        List<CreditPackgesRequestDTO> packages = cachePackgeService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<CreditPackgesRequestDTO> getPackageById(@PathVariable int packageId) {
        CreditPackgesRequestDTO creditPackage = cachePackgeService.getPackageById(packageId);
        return ResponseEntity.ok(creditPackage);
    }


}
