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
        String mensaje,
        String checkoutUrl
) {
    public PagoResponseDTO(
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
        this(id, suscripcionId, ordenId, monto, metodo, estado, fecha, referenciaExterna, mensaje, null);
    }

    public static PagoResponseDTO fromEntity(Pago pago, String mensaje) {
        return fromEntity(pago, mensaje, null);
    }

    public static PagoResponseDTO fromEntity(Pago pago, String mensaje, String checkoutUrl) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getSuscripcion() != null ? pago.getSuscripcion().getId() : null,
                pago.getOrdenId(),
                pago.getMonto(),
                pago.getMetodo(),
                pago.getEstado(),
                pago.getFecha(),
                pago.getReferenciaExterna(),
                mensaje,
                checkoutUrl
        );
    }
}
