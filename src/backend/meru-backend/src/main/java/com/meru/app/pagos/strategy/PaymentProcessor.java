package com.meru.app.pagos.strategy;

import com.meru.app.pagos.domain.MetodoPago;

public interface PaymentProcessor {
    ResultadoPago procesar(SolicitudPago solicitud);
    MetodoPago metodoSoportado();
}
