package com.meru.app.administracion.dto;

public record ActividadComposicionDTO(
        String tipoActividad,
        long cantidadAlumnos,
        long totalParticipaciones
) {}
