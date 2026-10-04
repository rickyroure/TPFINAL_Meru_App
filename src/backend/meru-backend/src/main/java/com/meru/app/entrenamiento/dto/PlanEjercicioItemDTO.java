package com.meru.app.entrenamiento.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PlanEjercicioItemDTO(
        @NotNull(message = "El ejercicioId es obligatorio")
        Long ejercicioId,

        @NotNull(message = "La sesión es obligatoria")
        @Pattern(regexp = "^[A-E]$", message = "La sesión debe ser una letra entre A y E")
        String sesion,

        @NotNull(message = "El número de semana es obligatorio")
        @Min(value = 1, message = "El número de semana debe ser al menos 1")
        Integer numeroSemana,

        Integer orden,

        @NotNull(message = "Las series son obligatorias")
        @Min(value = 1, message = "Debe haber al menos 1 serie")
        Integer series,

        @NotNull(message = "Las repeticiones son obligatorias")
        @Min(value = 1, message = "Debe haber al menos 1 repetición")
        Integer reps,

        Integer pausaSegundos,

        @DecimalMin(value = "0.0", message = "El porcentaje de carga no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de carga no puede superar 100")
        BigDecimal cargaPct,

        @DecimalMin(value = "1.0", message = "El RPE mínimo es 1")
        @DecimalMax(value = "10.0", message = "El RPE máximo es 10")
        BigDecimal rpe
) {}
