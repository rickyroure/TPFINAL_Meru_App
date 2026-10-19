package com.meru.app.shop.dto;

import com.meru.app.shop.domain.Producto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock
) {
    public static ProductoResponseDTO fromEntity(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock()
        );
    }
}
