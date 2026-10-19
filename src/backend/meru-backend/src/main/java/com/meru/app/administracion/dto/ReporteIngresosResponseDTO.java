package com.meru.app.administracion.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReporteIngresosResponseDTO(
        int anio,
        Integer mes,
        BigDecimal totalIngresos,
        BigDecimal ingresosSuscripciones,
        BigDecimal ingresosShop,
        long cantidadPagos,
        List<IngresoMensualDTO> desgloseMensual
) {}
