package com.meru.app.alumnos.dto;

import jakarta.validation.constraints.NotNull;

public record ActualizarAptoFisicoRequestDTO(
        @NotNull(message = "El estado de apto físico es obligatorio")
        Boolean aptoFisico
) {}
