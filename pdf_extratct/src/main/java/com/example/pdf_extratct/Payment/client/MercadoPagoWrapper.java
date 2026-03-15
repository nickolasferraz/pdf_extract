package com.example.pdf_extratct.Payment.client;

import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

public interface MercadoPagoWrapper {

    Preference createPreference(PreferenceRequest request);

    Payment createPayment(PaymentCreateRequest request, MPRequestOptions options);

    Payment getPayment(Long id);

}
