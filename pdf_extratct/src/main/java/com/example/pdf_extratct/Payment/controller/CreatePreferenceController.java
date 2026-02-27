package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.CreatePreferenceResponseDTO;
import com.example.pdf_extratct.Payment.dto.CreateReferenceRequestDto;
import com.example.pdf_extratct.Payment.service.CreatePaymentPreferenceService;
import com.mercadopago.exceptions.MPApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pdf_extratct.Payment.dto.CreatePreferenceResponseDTO;
import com.example.pdf_extratct.Payment.dto.CreateReferenceRequestDto;
import com.example.pdf_extratct.Payment.service.CreatePaymentPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class CreatePreferenceController {
    private final CreatePaymentPreferenceService createPaymentPreferenceService;
    @PostMapping("/preference")
    public ResponseEntity<CreatePreferenceResponseDTO> createPreference(
            @Validated
            @RequestBody CreateReferenceRequestDto request) throws MPApiException {
        try {
            CreatePreferenceResponseDTO response = createPaymentPreferenceService.createPreference(request);
            return ResponseEntity.ok(new CreatePreferenceResponseDTO(
                    response.preferenceId(),
                    response.redirectUrl()
            ));
        } catch (Exception e) {
            log.error("Erro creating Payment Preference: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}