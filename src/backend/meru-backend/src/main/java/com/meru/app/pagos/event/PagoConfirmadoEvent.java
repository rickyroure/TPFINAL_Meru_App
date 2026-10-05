package com.meru.app.pagos.event;

import java.math.BigDecimal;

public record PagoConfirmadoEvent(
        Long pagoId,
        Long alumnoId,
        BigDecimal monto,
        Long suscripcionId,
        Long ordenId
) {}
