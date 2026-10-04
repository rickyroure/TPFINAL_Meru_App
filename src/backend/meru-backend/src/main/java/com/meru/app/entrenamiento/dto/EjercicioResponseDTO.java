package com.meru.app.entrenamiento.dto;

import com.meru.app.entrenamiento.domain.Ejercicio;

public record EjercicioResponseDTO(
        Long id,
        String nombre,
        String grupoMuscular,
        String descripcion
) {
    public static EjercicioResponseDTO fromEntity(Ejercicio ejercicio) {
        return new EjercicioResponseDTO(
                ejercicio.getId(),
                ejercicio.getNombre(),
                ejercicio.getGrupoMuscular(),
                ejercicio.getDescripcion()
        );
    }
}
