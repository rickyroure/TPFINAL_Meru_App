package com.meru.app.pagos.dto;

import com.meru.app.pagos.domain.Plan;

import java.math.BigDecimal;

public record PlanResponseDTO(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer duracionDias
) {
    public static PlanResponseDTO fromEntity(Plan plan) {
        return new PlanResponseDTO(
                plan.getId(),
                plan.getNombre(),
                plan.getPrecio(),
                plan.getDuracionDias()
        );
    }
}
