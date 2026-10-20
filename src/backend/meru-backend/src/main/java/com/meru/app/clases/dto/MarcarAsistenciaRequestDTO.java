package com.meru.app.clases.dto;

import jakarta.validation.constraints.NotNull;

public record MarcarAsistenciaRequestDTO(
        @NotNull(message = "Debe indicar si el alumno asistió o no a la clase")
        Boolean asistio
) {}
