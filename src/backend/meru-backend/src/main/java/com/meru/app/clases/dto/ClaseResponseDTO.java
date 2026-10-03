package com.meru.app.clases.dto;

import com.meru.app.clases.domain.Clase;

import java.time.LocalDateTime;

public record ClaseResponseDTO(
        Long id,
        Long profesorId,
        String nombreProfesor,
        String tipoActividad,
        LocalDateTime horario,
        Integer cupoMaximo,
        Integer cupoDisponible
) {
    public static ClaseResponseDTO fromEntity(Clase clase) {
        String nombreProf = null;
        if (clase.getProfesor() != null && clase.getProfesor().getUsuario() != null) {
            nombreProf = clase.getProfesor().getUsuario().getNombre() + " " + clase.getProfesor().getUsuario().getApellido();
        }
        return new ClaseResponseDTO(
                clase.getId(),
                clase.getProfesor() != null ? clase.getProfesor().getId() : null,
                nombreProf,
                clase.getTipoActividad(),
                clase.getHorario(),
                clase.getCupoMaximo(),
                clase.getCupoDisponible()
        );
    }
}
