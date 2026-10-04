package com.meru.app.entrenamiento.dto;

import com.meru.app.entrenamiento.domain.Alumno1RM;
import java.math.BigDecimal;
import java.time.LocalDate;

public record Alumno1RMResponseDTO(
        Long id,
        Long alumnoId,
        Long ejercicioId,
        String nombreEjercicio,
        BigDecimal valor1rm,
        LocalDate fechaMedicion
) {
    public static Alumno1RMResponseDTO fromEntity(Alumno1RM alumno1RM) {
        return new Alumno1RMResponseDTO(
                alumno1RM.getId(),
                alumno1RM.getAlumno().getId(),
                alumno1RM.getEjercicio().getId(),
                alumno1RM.getEjercicio().getNombre(),
                alumno1RM.getValor1rm(),
                alumno1RM.getFechaMedicion()
        );
    }
}
