package com.example.pdf_extratct.Payment.client;

import com.example.pdf_extratct.Payment.config.MPRequestOptionsFactory;
import com.example.pdf_extratct.Payment.dto.*;
import com.example.pdf_extratct.Payment.models.entity.Payer;
import com.example.pdf_extratct.Payment.models.entity.PaymentEntity;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class MercadoPagoClient {
    @Value("${api.v1.mercadopago-access-token-vendedor}")
    private String accessToken;
    @Value("${api.v1.mercadopago-notification-url}")
    private String notificationUrl;

    @PostConstruct
    public void init(){
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado API Conectado");
    }

    public CreatePreferenceResponseDTO createpreference(CreateReferenceRequestDto inputdata, String orderNumber)
            throws MPException, MPApiException {

        PreferenceClient preferenceClient = new PreferenceClient();
        MPRequestOptions requestOptions = MPRequestOptionsFactory.createWithIdempotencyKey();

        try {
            List<PreferenceItemRequest> items = inputdata.items().stream()
                    .map(item -> PreferenceItemRequest.builder()
                            .id(item.id())
                            .title(item.title())
                            .quantity(item.quantity())
                            .unitPrice(BigDecimal.valueOf(item.unitPrice()))
                            .build())
                    .toList();

            PreferenceBackUrlsRequest backUrlsRequest = PreferenceBackUrlsRequest.builder()
                    .success(inputdata.backUrls().success())
                    .failure(inputdata.backUrls().failure())
                    .pending(inputdata.backUrls().pending())
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrlsRequest)
                    .notificationUrl(notificationUrl)
                    .externalReference(orderNumber)
                    .build();

            Preference preference = preferenceClient.create(preferenceRequest, requestOptions);

            return new CreatePreferenceResponseDTO(preference.getClientId(), preference.getInitPoint());

        } catch (MPApiException e) {
            log.error("Erro ao criar preferencia do mercado pago api: {}", e.getApiResponse().getContent());
            throw new MPApiException("Erro ao criar preferencia do mercado pago api", e.getApiResponse());
        } catch (MPException e) {
            log.error("Erro ao criar preferencia do mercado pago: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar preferencia do mercado pago: {}", e.getMessage());
            throw new MPException("Erro ao criar preferencia do mercado pago", e);
        }
    }

    public PaymentEntity getPaymentStatus(long id ) throws MPException, MPApiException{
        PaymentClient paymentClient = new PaymentClient();
        Payment paymentMercadoPago = paymentClient.get(id);

        if(paymentMercadoPago == null){
            log.error("Pagamento Não encontrado");
            throw  new MPException("Pagamento não encontrado");
        }

        String status = paymentMercadoPago.getStatus();
        String paymentMethod= paymentMercadoPago.getPaymentMethodId();

        log.info("Status do pagamento: {} e metodo de pagamento: {}",status,paymentMethod);

        Payer payer = buildPayerFromPayment(paymentMercadoPago);

        return PaymentEntity.builder()
                .id(paymentMercadoPago.getId().toString())
                .orderId(paymentMercadoPago.getExternalReference())
                .status(status)
                .amount(paymentMercadoPago.getTransactionAmount() != null ? paymentMercadoPago.getTransactionAmount().toString() : null)
                .statusDetail(paymentMercadoPago.getStatusDetail())
                .payer(payer)
                .payementMethodId(paymentMethod)
                .build();
    }

    public PaymentCreateResponsetDTO processpayment(CardPaymentDTO inputdata) throws MPException, MPApiException{
        try {
            PaymentClient client= new PaymentClient();
            MPRequestOptions mpRequestOptions = MPRequestOptionsFactory.createWithIdempotencyKey();

            String identificationNumber = inputdata.getPayer().getIdentification().getNumber();
            if (identificationNumber != null) {
                identificationNumber = identificationNumber.replaceAll("[^0-9]", "");
            }

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(inputdata.getTransactionAmount())
                    .token(inputdata.getToken())
                    .description(inputdata.getProductDescription())
                    .installments(inputdata.getInstallments())
                    .paymentMethodId(inputdata.getPaymentMethodId())
                    .payer(
                            PaymentPayerRequest.builder()
                                    .email(inputdata.getPayer().getEmail())
                                    .firstName(inputdata.getPayer().getFirstName())
                                    .identification(
                                            IdentificationRequest.builder()
                                                    .type(inputdata.getPayer().getIdentification().getType())
                                                    .number(identificationNumber)
                                                    .build())
                                    .build()
                    ).build();

            Payment payment = client.create(paymentCreateRequest, mpRequestOptions);

            return new PaymentCreateResponsetDTO(
                    payment.getId(),
                    payment.getTransactionAmount(),
                    inputdata.getToken(),
                    payment.getDescription(),
                    payment.getInstallments(),
                    payment.getPaymentMethodId(),
                    buildPayerFromPayment(payment)
            );

        } catch (MPApiException e) {
            String errorDetails = e.getApiResponse() != null ? e.getApiResponse().getContent() : "Sem detalhes da resposta";
            log.error("Erro na API do Mercado Pago ao processar pagamento com cartão. Status: {}. Detalhes: {}", e.getStatusCode(), errorDetails);
            throw new RuntimeException("Erro ao processar pagamento com cartão: " + errorDetails, e);
        } catch (MPException e) {
            log.error("Error processing payment with Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error processing payment", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing payment: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while processing payment", e);
        }
    }

    private Payer buildPayerFromPayment(Payment payment) {
        if (payment.getPayer() != null && payment.getPayer().getEmail() != null) {
            return Payer.builder()
                    .email(payment.getPayer().getEmail())
                    .firstName(payment.getPayer().getFirstName())
                    .identification(Payer.Identification.builder()
                            .type(payment.getPayer().getIdentification().getType())
                            .number(payment.getPayer().getIdentification().getNumber())
                            .build())
                    .build();
        }
        return null;
    }

    public PixPaymentResponseDTO createPixPayment(PixPaymentRequestDTO inputdata) throws MPException, MPApiException {
        try {
            PaymentClient client = new PaymentClient();
            MPRequestOptions mpRequestOptions = MPRequestOptionsFactory.createWithIdempotencyKey();

            String identificationNumber = inputdata.payer().identification().number();
            if (identificationNumber != null) {
                identificationNumber = identificationNumber.replaceAll("[^0-9]", "");
            }

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(inputdata.transactionAmount())
                    .description(inputdata.description())
                    .paymentMethodId("pix")
                    .payer(
                            PaymentPayerRequest.builder()
                                    .email(inputdata.payer().email())
                                    .firstName(inputdata.payer().firstName())
                                    .lastName(inputdata.payer().lastName())
                                    .identification(
                                            IdentificationRequest.builder()
                                                    .type(inputdata.payer().identification().type())
                                                    .number(identificationNumber)
                                                    .build())
                                    .build()
                    ).build();

            Payment payment = client.create(paymentCreateRequest, mpRequestOptions);

            return mapToPixResponseDTO(payment);

        } catch (MPApiException e) {
            // Captura o conteúdo detalhado do erro da API
            String errorDetails = e.getApiResponse() != null ? e.getApiResponse().getContent() : "Sem detalhes da resposta";
            log.error("Erro na API do Mercado Pago ao criar Pix. Status: {}. Detalhes: {}", e.getStatusCode(), errorDetails);
            
            // Lança a exceção com os detalhes para que apareça na resposta da sua API
            throw new RuntimeException("Erro ao criar pagamento Pix: " + errorDetails, e);
        } catch (MPException e) {
            log.error("Erro no SDK do Mercado Pago ao criar Pix: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar pagamento Pix", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao criar Pix: {}", e.getMessage());
            throw new RuntimeException("Erro inesperado ao criar Pix", e);
        }
    }

    private PixPaymentResponseDTO mapToPixResponseDTO(Payment payment) {
        var poi = payment.getPointOfInteraction();
        var transactionData = poi != null ? poi.getTransactionData() : null;

        return new PixPaymentResponseDTO(
                payment.getId(),
                payment.getStatus(),
                payment.getStatusDetail(),
                new PixPaymentResponseDTO.PointOfInteractionDTO(
                        new PixPaymentResponseDTO.TransactionDataDTO(
                                transactionData != null ? transactionData.getQrCode() : null,
                                transactionData != null ? transactionData.getQrCodeBase64() : null,
                                transactionData != null ? transactionData.getTicketUrl() : null
                        )
                )
        );
    }
}
