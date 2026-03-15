package com.example.pdf_extratct.readpdf.dto;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.Builder;

@Builder
public record PdfProcessingRequest(
        String headers,
        String fileName,
        Long fileSize,
        UserEntity user,
        String clientIp
) {
    public boolean isAnonymous() {
        return user == null;
    }
}
