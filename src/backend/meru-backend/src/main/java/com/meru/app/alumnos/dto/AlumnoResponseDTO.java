package com.meru.app.alumnos.dto;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.profesores.dto.ProfesorResumenDTO;
import java.time.LocalDate;

public record AlumnoResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        boolean aptoFisico,
        LocalDate fechaIngreso,
        ProfesorResumenDTO profesor
) {
    public static AlumnoResponseDTO fromEntity(Alumno alumno) {
        return new AlumnoResponseDTO(
                alumno.getId(),
                alumno.getUsuario().getNombre(),
                alumno.getUsuario().getApellido(),
                alumno.getUsuario().getEmail(),
                alumno.isAptoFisico(),
                alumno.getFechaIngreso(),
                ProfesorResumenDTO.fromEntity(alumno.getProfesor())
        );
    }
}
