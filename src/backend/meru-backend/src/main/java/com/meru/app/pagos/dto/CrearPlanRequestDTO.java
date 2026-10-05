package com.meru.app.pagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearPlanRequestDTO(
        @NotBlank(message = "El nombre del plan es obligatorio")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precio,

        @NotNull(message = "La duración en días es obligatoria")
        @Min(value = 1, message = "La duración mínima es de 1 día")
        Integer duracionDias
) {}
