package com.meru.app.entrenamiento.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AsignarRutinaRequestDTO(
        @NotNull(message = "El alumnoId es obligatorio")
        Long alumnoId,

        LocalDate fechaInicio
) {}
