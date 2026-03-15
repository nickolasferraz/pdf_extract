package com.example.pdf_extratct.Payment.dto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ExternalReference(String userId, int packageId) {

    public static ExternalReference parse(String externalRef) {
        if (externalRef == null || !externalRef.contains("|")) {
            log.warn("external_reference inválido ou não possui packageId. Ref: {}", externalRef);
            return null;
        }

        String[] parts = externalRef.split("\\|");

        try {
            int packageId = Integer.parseInt(parts[1]);
            return new ExternalReference(parts[0], packageId);
        } catch (NumberFormatException e) {
            log.error("Formato inválido do packageId no external_reference: {}", externalRef);
            return null;
        }
    }
}
