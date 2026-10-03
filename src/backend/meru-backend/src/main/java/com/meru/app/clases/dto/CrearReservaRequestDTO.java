package com.meru.app.clases.dto;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequestDTO(
        @NotNull(message = "El claseId es obligatorio")
        Long claseId,

        @NotNull(message = "El alumnoId es obligatorio")
        Long alumnoId
) {}
