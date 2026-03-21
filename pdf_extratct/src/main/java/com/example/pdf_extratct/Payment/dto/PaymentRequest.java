package com.example.pdf_extratct.Payment.dto;

import com.example.pdf_extratct.Payment.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentRequest(

        // === COMUNS A TODOS ===
        PaymentType paymentType,                                                    // discriminador: define qual strategy será usada
        @JsonProperty("transaction_amount") BigDecimal transactionAmount,           // valor do pagamento
        @JsonProperty("package_id") Integer packageId,                             // pacote de créditos a ser comprado
        String description,                                                         // descrição exibida no comprovante
        PayerDTO payer,                                                             // dados do pagador (email, nome, documento)

        // === SÓ CREDIT_CARD ===
        CardDetails cardDetails,                                                    // null para PIX e Checkout Pro

        // === SÓ CHECKOUT_PRO ===
        CheckoutProDetails checkoutProDetails                                       // null para PIX e Cartão
) {

    // Dados do pagador — comuns a todos os tipos de pagamento
    public record PayerDTO(
            String email,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            IdentificationDTO identification                                        // CPF/CNPJ do pagador
    ) {}

    // Documento de identificação do pagador
    public record IdentificationDTO(
            String type,    // ex: "CPF", "CNPJ"
            String number   // número do documento (somente dígitos)
    ) {}

    // Dados específicos do pagamento com cartão de crédito
    public record CardDetails(
            String token,                                                           // token gerado pelo SDK do MP no frontend
            Integer installments,                                                   // número de parcelas
            @JsonProperty("payment_method_id") String paymentMethodId,             // ex: "visa", "master"
            String issuerId                                                         // emissor do cartão
    ) {}

    // Dados específicos do Checkout Pro (redirect para página do MP)
    public record CheckoutProDetails(
            List<ItemsDto> items,                                                   // lista de produtos/pacotes
            BackUrlsDto backUrlsDto                                                 // URLs de retorno após o pagamento
    ) {}

    // Item do pedido para o Checkout Pro
    public record ItemsDto(
            String id,
            String title,
            int quantity,
            int unitPrice
    ) {}

    // URLs de retorno após conclusão do Checkout Pro
    public record BackUrlsDto(
            String success,     // redireciona aqui se aprovado
            String failure,     // redireciona aqui se recusado
            String pending      // redireciona aqui se pendente
    ) {}

}

