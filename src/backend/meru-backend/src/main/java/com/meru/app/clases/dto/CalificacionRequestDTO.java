package com.meru.app.clases.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalificacionRequestDTO(
        @NotNull(message = "El claseId es obligatorio")
        Long claseId,

        @NotNull(message = "El alumnoId es obligatorio")
        Long alumnoId,

        @NotNull(message = "El puntaje es obligatorio")
        @Min(value = 1, message = "El puntaje mínimo es 1")
        @Max(value = 5, message = "El puntaje máximo es 5")
        Integer puntaje,

        String comentario
) {}
