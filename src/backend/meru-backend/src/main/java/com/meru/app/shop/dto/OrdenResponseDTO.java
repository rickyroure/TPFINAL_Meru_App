package com.meru.app.shop.dto;

import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.Orden;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponseDTO(
        Long id,
        Long alumnoId,
        BigDecimal total,
        EstadoOrden estado,
        LocalDateTime fecha,
        List<ItemOrdenResponseDTO> items
) {
    public static OrdenResponseDTO fromEntity(Orden orden) {
        List<ItemOrdenResponseDTO> itemsDto = orden.getItems() != null
                ? orden.getItems().stream().map(ItemOrdenResponseDTO::fromEntity).toList()
                : List.of();
        return new OrdenResponseDTO(
                orden.getId(),
                orden.getAlumno() != null ? orden.getAlumno().getId() : null,
                orden.getTotal(),
                orden.getEstado(),
                orden.getFecha(),
                itemsDto
        );
    }
}
