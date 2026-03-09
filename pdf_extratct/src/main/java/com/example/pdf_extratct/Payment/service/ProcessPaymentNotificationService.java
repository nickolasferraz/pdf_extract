package com.example.pdf_extratct.Payment.service;


import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.ProcessNotificationResponseDTO;
import com.example.pdf_extratct.Payment.models.entity.PaymentEntity;
import com.example.pdf_extratct.Payment.CreditPackgesRepository;
import com.example.pdf_extratct.Payment.models.entity.CreditPackagesEntity;
import com.example.pdf_extratct.loginpage.credittransaction.CreditService;
import com.example.pdf_extratct.loginpage.credittransaction.TransactionType;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProcessPaymentNotificationService {

    private final MercadoPagoClient mercadoPagoClient;
    private final UserRepository userRepository;
    private final CreditPackgesRepository creditPackgesRepository;
    private final CreditService creditService;

    public ProcessPaymentNotificationService(MercadoPagoClient mercadoPagoClient, 
                                             UserRepository userRepository, 
                                             CreditPackgesRepository creditPackgesRepository,
                                             CreditService creditService) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.userRepository = userRepository;
        this.creditPackgesRepository = creditPackgesRepository;
        this.creditService = creditService;
    }

    @Transactional
    public ProcessNotificationResponseDTO processNotification(String id, String type){

        log.info("Processing paymet notification with id:{} and type: {}",id,type);

        try {
            PaymentEntity payment = mercadoPagoClient.getPaymentStatus(Long.parseLong(id));
            log.info("Notification processed sucessfully for id: {}" , id);

            if ("approved".equals(payment.getStatus())) {
                String externalRef = payment.getExternalReference();
                
                if (externalRef != null && externalRef.contains("|")) {
                    String[] parts = externalRef.split("\\|");
                    String userId = parts[0];
                    int packageId;
                    
                    try {
                        packageId = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        log.error("Formato inválido do packageId no external_reference: {}", externalRef);
                        return new ProcessNotificationResponseDTO(true, payment.getStatus());
                    }

                    log.info("Extracted userId: {} and packageId: {} from external_reference", userId, packageId);
                    
                    userRepository.findByUserId(userId).ifPresent(user -> {
                        CreditPackagesEntity creditPackage = creditPackgesRepository.findByPackageId(packageId);
                        
                        if (creditPackage != null) {
                            int creditsToAdd = creditPackage.getCredits();
                            String description = "Compra de Créditos - Pacote " + creditPackage.getName();
                            
                            creditService.addCredits(
                                    user,
                                    creditsToAdd,
                                    TransactionType.PURCHASE,
                                    description
                            );
                            
                            log.info("Adicionado {} créditos para o usuário {} referentes ao pacote ID {} e transação PURCHASE salva", creditsToAdd, user.getEmail(), packageId);
                        } else {
                            log.error("Pacote ID {} não encontrado no banco de dados para a atribuição de créditos", packageId);
                        }
                    });
                } else {
                    log.warn("Pagamento aprovado, mas external_reference inválido ou não possui packageId. Ref: {}", externalRef);
                }
            }

            return new ProcessNotificationResponseDTO(true, payment.getStatus());
        }
        catch (Exception e){
            log.error("Error processing notification for id {}: {}",id,e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
