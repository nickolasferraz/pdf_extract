package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.CreatePreferenceResponseDTO;
import com.example.pdf_extratct.Payment.dto.CreateReferenceRequestDto;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class CreatePaymentPreferenceService {

    private final MercadoPagoClient mercadoPagoClient;

    public CreatePaymentPreferenceService(MercadoPagoClient mercadoPagoClient) {
        this.mercadoPagoClient = mercadoPagoClient;
    }

    public CreatePreferenceResponseDTO createPreference(CreateReferenceRequestDto inputdata, String userId){
        log.info("Creating payment preference for mercado pago api:{} with local userId: {}",inputdata.toString(), userId);
        //validar Resquest
        //validar usuário
        //validar produto
        //validar se o total do pedido bate com o total do banco de dados
        //validar

        String  orderNumber="123123123";

        try{

            return  mercadoPagoClient.createpreference(inputdata, orderNumber, userId, inputdata.packageId());
        }
        catch (MPException e){
            log.info("Erro ao Criar Preferência de pagamento:{} ",e.getMessage());
            throw new RuntimeException(e);
        }catch (MPApiException e){
            log.info("Erro ao Criar Pagamento:{} ",e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
