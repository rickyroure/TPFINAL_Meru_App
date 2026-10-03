package com.meru.app.clases.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CrearClaseRequestDTO(
        @NotNull(message = "El profesorId es obligatorio")
        Long profesorId,

        @NotBlank(message = "El tipo de actividad es obligatorio")
        String tipoActividad,

        @NotNull(message = "El horario es obligatorio")
        @Future(message = "El horario debe ser en el futuro")
        LocalDateTime horario,

        @NotNull(message = "El cupo máximo es obligatorio")
        @Min(value = 1, message = "El cupo máximo debe ser al menos 1")
        Integer cupoMaximo
) {}
