package com.meru.app.clases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CheckinRequestDTO(
        @NotNull(message = "El alumnoId es obligatorio")
        Long alumnoId,

        @NotBlank(message = "La actividad es obligatoria")
        String actividad,

        LocalDateTime fechaHora
) {}
