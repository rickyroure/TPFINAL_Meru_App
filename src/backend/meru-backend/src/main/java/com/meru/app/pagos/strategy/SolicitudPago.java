package com.meru.app.pagos.strategy;

import com.meru.app.pagos.domain.MetodoPago;

import java.math.BigDecimal;

public record SolicitudPago(
        BigDecimal monto,
        MetodoPago metodo,
        Long suscripcionId,
        Long ordenId
) {}
