package com.meru.app.pagos.dto;

import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Suscripcion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record SuscripcionResponseDTO(
        Long id,
        Long alumnoId,
        String nombreAlumno,
        Long planId,
        String nombrePlan,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSuscripcion estado,
        Long diasRestantes
) {
    public SuscripcionResponseDTO(
            Long id,
            Long alumnoId,
            String nombreAlumno,
            Long planId,
            String nombrePlan,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            EstadoSuscripcion estado
    ) {
        this(id, alumnoId, nombreAlumno, planId, nombrePlan, fechaInicio, fechaFin, estado,
                calcularDiasRestantes(fechaFin, estado));
    }

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
                suscripcion.getEstado(),
                calcularDiasRestantes(suscripcion.getFechaFin(), suscripcion.getEstado())
        );
    }

    private static long calcularDiasRestantes(LocalDate fechaFin, EstadoSuscripcion estado) {
        if (fechaFin == null || estado != EstadoSuscripcion.ACTIVA) {
            return 0L;
        }
        return Math.max(0L, ChronoUnit.DAYS.between(LocalDate.now(), fechaFin));
    }
}

