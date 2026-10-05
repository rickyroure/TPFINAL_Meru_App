package com.meru.app.pagos.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CrearSuscripcionRequestDTO(
        @NotNull(message = "El alumnoId es obligatorio")
        Long alumnoId,

        @NotNull(message = "El planId es obligatorio")
        Long planId,

        LocalDate fechaInicio
) {}
