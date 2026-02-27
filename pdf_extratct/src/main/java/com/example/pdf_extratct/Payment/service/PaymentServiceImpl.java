package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.client.MercadoPagoClient;
import com.example.pdf_extratct.Payment.dto.CardPaymentDTO;
import com.example.pdf_extratct.Payment.dto.PaymentCreateResponsetDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentRequestDTO;
import com.example.pdf_extratct.Payment.dto.PixPaymentResponseDTO;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final MercadoPagoClient mercadoPagoClient;
    // private final PaymentRepository paymentRepository; // Você injetaria seu repositório aqui

    @Override
    public PaymentCreateResponsetDTO processPayment(CardPaymentDTO cardPaymentDTO) throws MPException, MPApiException {
        log.info("Processing payment for payer: {}", cardPaymentDTO.getPayer().getEmail());

        // A lógica de chamar o cliente de pagamento foi movida para cá
        PaymentCreateResponsetDTO paymentResponse = mercadoPagoClient.processpayment(cardPaymentDTO);

        // --- LÓGICA DE NEGÓCIO ADICIONAL IRIA AQUI ---
        // Exemplo:
        // 1. Converter o DTO de resposta para a sua PaymentEntity
        // PaymentEntity paymentEntity = mapToEntity(paymentResponse);
        //
        // 2. Salvar a entidade no banco de dados
        // paymentRepository.save(paymentEntity);
        // log.info("Payment with ID {} saved successfully.", paymentEntity.getId());
        //
        // 3. Enviar uma notificação, atualizar um pedido, etc.

        return paymentResponse;
    }

    @Override
    public PixPaymentResponseDTO createPixPayment(PixPaymentRequestDTO request) throws MPException, MPApiException {
        log.info("Processing Pix payment for payer: {}", request.payer().email());
        return mercadoPagoClient.createPixPayment(request);
    }
}
