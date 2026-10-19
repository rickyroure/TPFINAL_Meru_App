package com.meru.app.shop.dto;

import com.meru.app.shop.domain.ItemOrden;

import java.math.BigDecimal;

public record ItemOrdenResponseDTO(
        Long id,
        Long productoId,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    public static ItemOrdenResponseDTO fromEntity(ItemOrden item) {
        BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
        return new ItemOrdenResponseDTO(
                item.getId(),
                item.getProducto() != null ? item.getProducto().getId() : null,
                item.getProducto() != null ? item.getProducto().getNombre() : null,
                item.getCantidad(),
                item.getPrecioUnitario(),
                subtotal
        );
    }
}
