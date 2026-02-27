package com.example.pdf_extratct.Payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MercadoPagoConfigDTO{
    private  String action;
    private  String api_version;
    private  MercadoPagoData data;
    private  String data_created;
    private  String id;
    private  boolean live_mode;
    private  String type;
    private  long user_id;

    @Data
    @NoArgsConstructor
    public static class MercadoPagoData{
        private String id;
    }

}