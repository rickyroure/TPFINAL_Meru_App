package com.meru.app.clases.dto;

import com.meru.app.clases.domain.Calificacion;

public record CalificacionResponseDTO(
        Long id,
        Long claseId,
        Long alumnoId,
        String nombreAlumno,
        Integer puntaje,
        String comentario
) {
    public static CalificacionResponseDTO fromEntity(Calificacion calificacion) {
        String nombreAl = null;
        if (calificacion.getAlumno() != null && calificacion.getAlumno().getUsuario() != null) {
            nombreAl = calificacion.getAlumno().getUsuario().getNombre() + " " + calificacion.getAlumno().getUsuario().getApellido();
        }
        return new CalificacionResponseDTO(
                calificacion.getId(),
                calificacion.getClase() != null ? calificacion.getClase().getId() : null,
                calificacion.getAlumno() != null ? calificacion.getAlumno().getId() : null,
                nombreAl,
                calificacion.getPuntaje(),
                calificacion.getComentario()
        );
    }
}
