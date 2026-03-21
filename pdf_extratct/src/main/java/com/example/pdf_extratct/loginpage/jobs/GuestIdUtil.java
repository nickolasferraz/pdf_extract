package com.example.pdf_extratct.loginpage.jobs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GuestIdUtil {

    // Salte sua aplicação (mova para application.properties e injetar)
    private static final String SALT = "replace-this-with-secure-random-salt-from-config";

    public static String toGuestId(String clientIp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String raw = clientIp + "::" + SALT;
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            // Base64 URL-safe sem padding (opcional)
            return "guest:" + Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
