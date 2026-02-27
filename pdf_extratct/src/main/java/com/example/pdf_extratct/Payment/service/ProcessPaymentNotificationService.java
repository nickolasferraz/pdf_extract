package com.example.pdf_extratct.Payment.service;


import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.ProcessNotificationResponseDTO;
import com.example.pdf_extratct.Payment.models.entity.PaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessPaymentNotificationService {

    private final MercadoPagoClient mercadoPagoClient;

    public ProcessPaymentNotificationService(MercadoPagoClient mercadoPagoClient) {
        this.mercadoPagoClient = mercadoPagoClient;
    }

    public ProcessNotificationResponseDTO processNotification(String id, String type){

        log.info("Processing paymet notification with id:{} and type: {}",id,type);

        try {
            PaymentEntity payment = mercadoPagoClient.getPaymentStatus(Long.parseLong(id));
            log.info("Notification processed sucessfully for id: {}" , id);


            return new ProcessNotificationResponseDTO(true, payment.getStatus());
        }
        catch (Exception e){
            log.error("Error processing notification for id {}: {}",id,e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
