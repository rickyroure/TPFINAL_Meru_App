package com.meru.app.administracion.dto;

import java.math.BigDecimal;

public record IngresoMensualDTO(
        int anio,
        int mes,
        BigDecimal totalIngresos,
        BigDecimal ingresosSuscripciones,
        BigDecimal ingresosShop,
        long cantidadPagos
) {}
