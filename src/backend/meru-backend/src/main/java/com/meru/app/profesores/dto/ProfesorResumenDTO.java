package com.meru.app.profesores.dto;

import com.meru.app.profesores.domain.Profesor;

public record ProfesorResumenDTO(
        Long id,
        String nombre,
        String apellido,
        String especialidad
) {
    public static ProfesorResumenDTO fromEntity(Profesor profesor) {
        if (profesor == null) {
            return null;
        }
        return new ProfesorResumenDTO(
                profesor.getId(),
                profesor.getUsuario().getNombre(),
                profesor.getUsuario().getApellido(),
                profesor.getEspecialidad()
        );
    }
}
