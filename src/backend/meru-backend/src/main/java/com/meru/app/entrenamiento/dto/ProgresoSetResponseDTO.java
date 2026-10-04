package com.meru.app.entrenamiento.dto;

import com.meru.app.entrenamiento.domain.ProgresoSet;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgresoSetResponseDTO(
        Long id,
        Long planEjercicioId,
        Integer numeroSerie,
        BigDecimal pesoRealizado,
        Integer repeticionesRealizadas,
        LocalDate fecha
) {
    public static ProgresoSetResponseDTO fromEntity(ProgresoSet progreso) {
        return new ProgresoSetResponseDTO(
                progreso.getId(),
                progreso.getPlanEjercicio().getId(),
                progreso.getNumeroSerie(),
                progreso.getPesoRealizado(),
                progreso.getRepeticionesRealizadas(),
                progreso.getFecha()
        );
    }
}
