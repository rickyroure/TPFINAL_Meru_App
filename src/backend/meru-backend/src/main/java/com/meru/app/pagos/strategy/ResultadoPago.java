package com.meru.app.pagos.strategy;

import com.meru.app.pagos.domain.EstadoPago;

public record ResultadoPago(
        EstadoPago estado,
        String referenciaExterna,
        String mensaje
) {}
