package com.meru.app.pagos.dto;

import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Suscripcion;

import java.time.LocalDate;

public record SuscripcionResponseDTO(
        Long id,
        Long alumnoId,
        String nombreAlumno,
        Long planId,
        String nombrePlan,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSuscripcion estado
) {
    public static SuscripcionResponseDTO fromEntity(Suscripcion suscripcion) {
        String nombreAl = null;
        if (suscripcion.getAlumno() != null && suscripcion.getAlumno().getUsuario() != null) {
            nombreAl = suscripcion.getAlumno().getUsuario().getNombre() + " " + suscripcion.getAlumno().getUsuario().getApellido();
        }
        return new SuscripcionResponseDTO(
                suscripcion.getId(),
                suscripcion.getAlumno() != null ? suscripcion.getAlumno().getId() : null,
                nombreAl,
                suscripcion.getPlan() != null ? suscripcion.getPlan().getId() : null,
                suscripcion.getPlan() != null ? suscripcion.getPlan().getNombre() : null,
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                suscripcion.getEstado()
        );
    }
}
