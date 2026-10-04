package com.meru.app.entrenamiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EjercicioRequestDTO(
        @NotBlank(message = "El nombre del ejercicio es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 100, message = "El grupo muscular no puede superar los 100 caracteres")
        String grupoMuscular,

        String descripcion
) {}
