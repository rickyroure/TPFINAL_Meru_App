package com.meru.app.entrenamiento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearRutinaRequestDTO(
        @NotNull(message = "El profesorId es obligatorio")
        Long profesorId,

        @NotBlank(message = "El nombre de la rutina es obligatorio")
        String nombre,

        @NotNull(message = "La duración en semanas es obligatoria")
        @Min(value = 1, message = "La duración mínima es de 1 semana")
        Integer duracionSemanas,

        @Valid
        List<PlanEjercicioItemDTO> items
) {}
