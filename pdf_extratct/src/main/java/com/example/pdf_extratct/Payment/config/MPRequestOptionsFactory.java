package com.example.pdf_extratct.Payment.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.core.MPRequestOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MPRequestOptionsFactory {

    public static MPRequestOptions createWithIdempotencyKey() {


        return MPRequestOptions.builder()
                .accessToken(MercadoPagoConfig.getAccessToken())  // ← PRECISA ter isso!
                .customHeaders(Map.of("X-Idempotency-Key", UUID.randomUUID().toString()))
                .build();
    }
}
