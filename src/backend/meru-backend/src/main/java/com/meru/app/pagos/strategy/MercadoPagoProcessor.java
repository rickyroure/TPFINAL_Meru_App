package com.meru.app.pagos.strategy;

import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MercadoPagoProcessor implements PaymentProcessor {

    @Override
    public ResultadoPago procesar(SolicitudPago solicitud) {
        String preferenceId = "MP-PREF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ResultadoPago(EstadoPago.PENDIENTE, preferenceId, "Preferencia de MercadoPago generada");
    }

    @Override
    public MetodoPago metodoSoportado() {
        return MetodoPago.MERCADOPAGO;
    }
}
