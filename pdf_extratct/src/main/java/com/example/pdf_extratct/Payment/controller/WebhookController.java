package com.example.pdf_extratct.Payment.controller;

import com.example.pdf_extratct.Payment.dto.MercadoPagoConfigDTO;
import com.example.pdf_extratct.Payment.service.ProcessPaymentNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ProcessPaymentNotificationService processPayamentNotificationService;

    public WebhookController(ProcessPaymentNotificationService processPayamentNotificationService) {
        this.processPayamentNotificationService = processPayamentNotificationService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handlerNotification(@RequestBody MercadoPagoConfigDTO mercadoPagoConfigDTO){

        String resourceId= mercadoPagoConfigDTO.getData().getId();
        String resourceType= mercadoPagoConfigDTO.getType();

        try {
            var result=processPayamentNotificationService.processNotification(resourceId,resourceType);

            log.info("Webhook processed sucessfully for resource ID:{} and resource type:{}",result.sucess(),resourceType);

        }catch (Exception e){
            log.error("Erro ao processar notificação do mercado pago {}",e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();

    }
}
