package com.meru.app.pagos.strategy;

import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EfectivoProcessor implements PaymentProcessor {

    @Override
    public ResultadoPago procesar(SolicitudPago solicitud) {
        String reciboId = "REC-EFECTIVO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ResultadoPago(EstadoPago.APROBADO, reciboId, "Pago en efectivo recibido y aprobado");
    }

    @Override
    public MetodoPago metodoSoportado() {
        return MetodoPago.EFECTIVO;
    }
}
