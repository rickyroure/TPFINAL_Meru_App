package com.meru.app.clases.dto;

import com.meru.app.clases.domain.Checkin;

import java.time.LocalDateTime;

public record CheckinResponseDTO(
        Long id,
        Long alumnoId,
        String nombreAlumno,
        String actividad,
        LocalDateTime fechaHora
) {
    public static CheckinResponseDTO fromEntity(Checkin checkin) {
        String nombreAl = null;
        if (checkin.getAlumno() != null && checkin.getAlumno().getUsuario() != null) {
            nombreAl = checkin.getAlumno().getUsuario().getNombre() + " " + checkin.getAlumno().getUsuario().getApellido();
        }
        return new CheckinResponseDTO(
                checkin.getId(),
                checkin.getAlumno() != null ? checkin.getAlumno().getId() : null,
                nombreAl,
                checkin.getActividad(),
                checkin.getFechaHora()
        );
    }
}
