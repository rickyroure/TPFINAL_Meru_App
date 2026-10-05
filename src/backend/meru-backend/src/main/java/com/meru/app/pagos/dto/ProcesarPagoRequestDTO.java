package com.meru.app.pagos.dto;

import com.meru.app.pagos.domain.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProcesarPagoRequestDTO(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodo,

        Long suscripcionId,

        Long ordenId
) {}
