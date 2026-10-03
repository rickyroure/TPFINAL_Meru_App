package com.meru.app.alumnos.dto;

import jakarta.validation.constraints.NotNull;

public record AlumnoRequestDTO(
        @NotNull(message = "El ID de usuario es obligatorio")
        Long usuarioId,
        @NotNull(message = "Debe indicar si posee apto físico")
        Boolean aptoFisico
) {}
