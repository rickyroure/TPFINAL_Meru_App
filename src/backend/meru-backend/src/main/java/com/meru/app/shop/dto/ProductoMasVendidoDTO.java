package com.meru.app.shop.dto;

import java.math.BigDecimal;

public record ProductoMasVendidoDTO(
        Long productoId,
        String nombre,
        Integer stockActual,
        Long cantidadVendida,
        BigDecimal totalRecaudado
) {}
