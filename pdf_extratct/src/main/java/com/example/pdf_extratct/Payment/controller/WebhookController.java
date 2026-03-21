package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.MercadoPagoConfigDTO;
import com.example.pdf_extratct.Payment.service.ProcessPaymentNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ProcessPaymentNotificationService processPayamentNotificationService;

    public WebhookController(ProcessPaymentNotificationService processPayamentNotificationService) {
        this.processPayamentNotificationService = processPayamentNotificationService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handlerNotification(
            @RequestBody(required = false) MercadoPagoConfigDTO body,
            @RequestParam Map<String, String> params){

        String resourceId = null;
        String resourceType = null;

        // Try to get from body (Webhooks)
        if (body != null && body.getData() != null) {
            resourceId = body.getData().getId();
            resourceType = body.getType();
        } 
        
        // Fallback to query params (IPN)
        if (resourceId == null) {
            resourceId = params.get("id");
            resourceType = params.get("topic"); // IPN uses 'topic', Webhooks uses 'type'
        }

        if (resourceId == null) {
            log.warn("Recebido Webhook/IPN vazio ou sem ID. Params: {}", params);
            return ResponseEntity.ok().build(); // Retorna 200 para evitar retentativas infinitas de lixo
        }

        try {
            var result = processPayamentNotificationService.processNotification(resourceId, resourceType);
            log.info("Webhook processed sucessfully for resource ID:{} and resource type:{}", resourceId, resourceType);
        } catch (Exception e){
            log.error("Erro ao processar notificação do mercado pago {}: {}", resourceId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
