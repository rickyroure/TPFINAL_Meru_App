package com.meru.app.entrenamiento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarProgresoSetRequestDTO(
        @NotNull(message = "El planEjercicioId es obligatorio")
        Long planEjercicioId,

        @NotNull(message = "El número de serie es obligatorio")
        @Min(value = 1, message = "El número de serie debe ser mayor o igual a 1")
        Integer numeroSerie,

        @NotNull(message = "El peso realizado es obligatorio")
        @DecimalMin(value = "0.0", message = "El peso realizado no puede ser negativo")
        BigDecimal pesoRealizado,

        @Min(value = 0, message = "Las repeticiones realizadas no pueden ser negativas")
        Integer repeticionesRealizadas,

        LocalDate fecha
) {}
