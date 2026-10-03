package com.meru.app.profesores.dto;

import com.meru.app.profesores.domain.Profesor;

public record ProfesorResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String especialidad
) {
    public static ProfesorResponseDTO fromEntity(Profesor profesor) {
        return new ProfesorResponseDTO(
                profesor.getId(),
                profesor.getUsuario().getNombre(),
                profesor.getUsuario().getApellido(),
                profesor.getUsuario().getEmail(),
                profesor.getEspecialidad()
        );
    }
}
