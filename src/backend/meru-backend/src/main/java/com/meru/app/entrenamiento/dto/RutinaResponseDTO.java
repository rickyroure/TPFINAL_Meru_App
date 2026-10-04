package com.meru.app.entrenamiento.dto;

import com.meru.app.entrenamiento.domain.PlanEjercicio;
import com.meru.app.entrenamiento.domain.Rutina;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RutinaResponseDTO(
        Long id,
        Long profesorId,
        String nombreProfesor,
        Long alumnoId,
        String nombreAlumno,
        String nombre,
        LocalDate fechaInicio,
        Integer duracionSemanas,
        List<PlanEjercicioResponseDTO> items
) {
    public record PlanEjercicioResponseDTO(
            Long id,
            Long ejercicioId,
            String nombreEjercicio,
            String sesion,
            Integer numeroSemana,
            Integer orden,
            Integer series,
            Integer reps,
            Integer pausaSegundos,
            BigDecimal cargaPct,
            BigDecimal rpe
    ) {
        public static PlanEjercicioResponseDTO fromEntity(PlanEjercicio plan) {
            return new PlanEjercicioResponseDTO(
                    plan.getId(),
                    plan.getEjercicio().getId(),
                    plan.getEjercicio().getNombre(),
                    plan.getSesion(),
                    plan.getNumeroSemana(),
                    plan.getOrden(),
                    plan.getSeries(),
                    plan.getReps(),
                    plan.getPausaSegundos(),
                    plan.getCargaPct(),
                    plan.getRpe()
            );
        }
    }

    public static RutinaResponseDTO fromEntity(Rutina rutina) {
        List<PlanEjercicioResponseDTO> planDTOs = rutina.getPlanesEjercicio() != null
                ? rutina.getPlanesEjercicio().stream().map(PlanEjercicioResponseDTO::fromEntity).toList()
                : List.of();

        return new RutinaResponseDTO(
                rutina.getId(),
                rutina.getProfesor() != null ? rutina.getProfesor().getId() : null,
                rutina.getProfesor() != null && rutina.getProfesor().getUsuario() != null
                        ? rutina.getProfesor().getUsuario().getNombre() + " " + rutina.getProfesor().getUsuario().getApellido()
                        : null,
                rutina.getAlumno() != null ? rutina.getAlumno().getId() : null,
                rutina.getAlumno() != null && rutina.getAlumno().getUsuario() != null
                        ? rutina.getAlumno().getUsuario().getNombre() + " " + rutina.getAlumno().getUsuario().getApellido()
                        : null,
                rutina.getNombre(),
                rutina.getFechaInicio(),
                rutina.getDuracionSemanas(),
                planDTOs
        );
    }
}
