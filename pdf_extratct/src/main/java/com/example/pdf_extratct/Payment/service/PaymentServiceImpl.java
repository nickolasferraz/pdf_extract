package com.example.pdf_extratct.Payment.service;

import com.example.pdf_extratct.Payment.dto.PaymentRequest;
import com.example.pdf_extratct.Payment.dto.PaymentResult;
import com.example.pdf_extratct.Payment.factory.PaymentStrategyFactory;
import com.example.pdf_extratct.Payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory factory;

    @Override
    public PaymentResult processPayment(PaymentRequest request, String userId, Integer packageId) {

        log.info("Iniciando pagamento do tipo: {} para userId: {} | packageId: {}",
                request.paymentType(), userId, packageId);

        // A Factory seleciona a Strategy correta com base no paymentType
        PaymentStrategy strategy = factory.getStrategy(request.paymentType());

        // Delega a execução para a Strategy (Erros viram Runtime PaymentGatewayException)
        return strategy.execute(request, userId, packageId);
    }
}

