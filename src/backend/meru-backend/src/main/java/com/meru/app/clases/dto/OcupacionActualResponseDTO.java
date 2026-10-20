package com.meru.app.clases.dto;

import java.math.BigDecimal;

public record OcupacionActualResponseDTO(
        long personasPresentes,
        int capacidadMaximaReferencia,
        BigDecimal porcentajeOcupacion,
        String nivelOcupacion
) {}
