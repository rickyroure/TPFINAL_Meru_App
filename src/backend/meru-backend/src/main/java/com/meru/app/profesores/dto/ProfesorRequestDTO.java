package com.meru.app.profesores.dto;

import jakarta.validation.constraints.NotNull;

public record ProfesorRequestDTO(
        @NotNull(message = "El ID de usuario es obligatorio")
        Long usuarioId,
        String especialidad
) {}
