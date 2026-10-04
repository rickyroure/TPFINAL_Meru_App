package com.meru.app.entrenamiento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record Registrar1RMRequestDTO(
        @NotNull(message = "El ID del ejercicio es obligatorio")
        Long ejercicioId,

        @NotNull(message = "El valor del 1RM es obligatorio")
        @DecimalMin(value = "0.01", message = "El valor del 1RM debe ser mayor a 0")
        BigDecimal valor1rm,

        LocalDate fechaMedicion
) {}
