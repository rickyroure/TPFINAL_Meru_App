package com.meru.app.pagos.dto;

import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import com.meru.app.pagos.domain.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(
        Long id,
        Long suscripcionId,
        Long ordenId,
        BigDecimal monto,
        MetodoPago metodo,
        EstadoPago estado,
        LocalDateTime fecha,
        String referenciaExterna,
        String mensaje
) {
    public static PagoResponseDTO fromEntity(Pago pago, String mensaje) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getSuscripcion() != null ? pago.getSuscripcion().getId() : null,
                pago.getOrdenId(),
                pago.getMonto(),
                pago.getMetodo(),
                pago.getEstado(),
                pago.getFecha(),
                pago.getReferenciaExterna(),
                mensaje
        );
    }
}
