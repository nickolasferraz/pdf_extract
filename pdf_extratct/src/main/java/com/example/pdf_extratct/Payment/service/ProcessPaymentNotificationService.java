package com.example.pdf_extratct.Payment.service;


import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.ExternalReference;
import com.example.pdf_extratct.Payment.dto.ProcessNotificationResponseDTO;
import com.example.pdf_extratct.Payment.models.entity.PaymentEntity;
import com.example.pdf_extratct.loginpage.credittransaction.querys.CreditCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProcessPaymentNotificationService {

    private final MercadoPagoClient mercadoPagoClient;
    private final CreditCommandService creditCommandService;

    public ProcessPaymentNotificationService(MercadoPagoClient mercadoPagoClient,
                                             CreditCommandService creditCommandService) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.creditCommandService = creditCommandService;
    }

    @Transactional
    public ProcessNotificationResponseDTO processNotification(String id, String type) {

        log.info("Processing payment notification with id:{} and type: {}", id, type);

        try {
            PaymentEntity payment = mercadoPagoClient.getPaymentStatus(Long.parseLong(id));
            log.info("Notification processed successfully for id: {}", id);

            ProcessNotificationResponseDTO response = new ProcessNotificationResponseDTO(true, payment.getStatus());

            if (!"approved".equals(payment.getStatus())) {
                log.info("Pagamento não aprovado (status: {}), nenhuma ação necessária.", payment.getStatus());
                return response;
            }

            ExternalReference ref = ExternalReference.parse(payment.getExternalReference());
            if (ref == null) {
                return response;
            }

            creditCommandService.assignPurchaseCredits(ref.userId(), ref.packageId(), payment.getId());

            return response;

        } catch (Exception e) {
            log.error("Error processing notification for id {}: {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}



