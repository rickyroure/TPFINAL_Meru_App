package com.meru.app.administracion.dto;

public record AfluenciaRangoHorarioDTO(
        String rangoHorario,
        int horaInicio,
        long cantidadConcurrencias
) {}
