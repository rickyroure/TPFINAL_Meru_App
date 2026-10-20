package com.meru.app.alumnos.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ActualizarPerfilAlumnoRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        String telefono,

        LocalDate fechaNacimiento
) {}
