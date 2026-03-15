package com.example.pdf_extratct.Payment.client;

import com.example.pdf_extratct.Payment.exceptions.PaymentGatewayException;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MercadoPagoWrapperImpl implements MercadoPagoWrapper {

    @Value("${api.v1.mercadopago-access-token-vendedor}")
    private String accessToken;

    private PaymentClient paymentClient;
    private PreferenceClient preferenceClient;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        this.paymentClient = new PaymentClient();
        this.preferenceClient = new PreferenceClient();
        log.info("MercadoPago Wrapper inicializado");
    }

    @Override
    public Preference createPreference(PreferenceRequest request) {
        try {
            return preferenceClient.create(request);
        } catch (MPException | MPApiException e) {
            log.error("Erro no Mercado Pago SDK ao criar a Preference: {}", e.getMessage(), e);
            throw new PaymentGatewayException("Falha ao criar preferência de pagamento no gateway", e);
        }
    }

    @Override
    public Payment createPayment(PaymentCreateRequest request, MPRequestOptions options) {
        try {
             return paymentClient.create(request, options);
        } catch (MPException | MPApiException e) {
             log.error("Erro no Mercado Pago SDK ao processar Pagamento: {}", e.getMessage(), e);
             throw new PaymentGatewayException("Falha ao processar pagamento no gateway", e);
         }
    }

    @Override
    public Payment getPayment(Long id) {
        try {
             Payment payment = paymentClient.get(id);
             if(payment == null){
                 throw new PaymentGatewayException("Gateway retornou nulo para o pagamento: " + id);
             }
             return payment;
        } catch (MPException | MPApiException e) {
             log.error("Erro no Mercado Pago SDK ao buscar status do pagamento: {}", e.getMessage(), e);
             throw new PaymentGatewayException("Falha ao buscar status do pagamento no gateway", e);
         }
    }
}
