package com.meru.app.clases.dto;

import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;

import java.time.LocalDateTime;

public record ReservaResponseDTO(
        Long id,
        Long claseId,
        String tipoActividadClase,
        LocalDateTime horarioClase,
        Long alumnoId,
        String nombreAlumno,
        EstadoReserva estado,
        LocalDateTime fechaReserva
) {
    public static ReservaResponseDTO fromEntity(Reserva reserva) {
        String nombreAl = null;
        if (reserva.getAlumno() != null && reserva.getAlumno().getUsuario() != null) {
            nombreAl = reserva.getAlumno().getUsuario().getNombre() + " " + reserva.getAlumno().getUsuario().getApellido();
        }
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getClase() != null ? reserva.getClase().getId() : null,
                reserva.getClase() != null ? reserva.getClase().getTipoActividad() : null,
                reserva.getClase() != null ? reserva.getClase().getHorario() : null,
                reserva.getAlumno() != null ? reserva.getAlumno().getId() : null,
                nombreAl,
                reserva.getEstado(),
                reserva.getFechaReserva()
        );
    }
}
