package com.example.pdf_extratct.loginpage.jobs.creation;

import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.Builder;

@Builder
public record JobCreationContext(
        String fileName,
        Long fileSize,
        Integer estimatedCredits,
        UserEntity user,
        String clientIp
) {
}
