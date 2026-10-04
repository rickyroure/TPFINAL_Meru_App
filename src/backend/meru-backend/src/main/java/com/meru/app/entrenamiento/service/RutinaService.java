package com.meru.app.entrenamiento.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.domain.PlanEjercicio;
import com.meru.app.entrenamiento.domain.Rutina;
import com.meru.app.entrenamiento.dto.AsignarRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.CrearRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.PlanEjercicioItemDTO;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import com.meru.app.entrenamiento.repository.RutinaRepository;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;
    private final EjercicioRepository ejercicioRepository;

    public RutinaService(RutinaRepository rutinaRepository,
                         ProfesorRepository profesorRepository,
                         AlumnoRepository alumnoRepository,
                         EjercicioRepository ejercicioRepository) {
        this.rutinaRepository = rutinaRepository;
        this.profesorRepository = profesorRepository;
        this.alumnoRepository = alumnoRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @Transactional
    public Rutina crearRutinaPlantilla(CrearRutinaRequestDTO request) {
        Profesor profesor = profesorRepository.findById(request.profesorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con ID: " + request.profesorId()));

        Rutina rutina = new Rutina();
        rutina.setProfesor(profesor);
        rutina.setAlumno(null); // Rutina plantilla
        rutina.setNombre(request.nombre());
        rutina.setDuracionSemanas(request.duracionSemanas());

        if (request.items() != null) {
            for (PlanEjercicioItemDTO itemDTO : request.items()) {
                Ejercicio ejercicio = ejercicioRepository.findById(itemDTO.ejercicioId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado con ID: " + itemDTO.ejercicioId()));

                PlanEjercicio plan = new PlanEjercicio();
                plan.setEjercicio(ejercicio);
                plan.setSesion(itemDTO.sesion());
                plan.setNumeroSemana(itemDTO.numeroSemana());
                plan.setOrden(itemDTO.orden() != null ? itemDTO.orden() : 1);
                plan.setSeries(itemDTO.series());
                plan.setReps(itemDTO.reps());
                plan.setPausaSegundos(itemDTO.pausaSegundos());
                plan.setCargaPct(itemDTO.cargaPct());
                plan.setRpe(itemDTO.rpe());

                rutina.addPlanEjercicio(plan);
            }
        }

        return rutinaRepository.save(rutina);
    }

    @Transactional
    public Rutina asignarRutina(Long rutinaId, AsignarRutinaRequestDTO request) {
        Rutina rutinaOriginal = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rutina no encontrada con ID: " + rutinaId));

        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        // Decisión de diseño cerrada: clonar la rutina creando una nueva fila en RUTINA
        Rutina rutinaClonada = new Rutina();
        rutinaClonada.setProfesor(rutinaOriginal.getProfesor());
        rutinaClonada.setAlumno(alumno);
        rutinaClonada.setNombre(rutinaOriginal.getNombre());
        rutinaClonada.setFechaInicio(request.fechaInicio() != null ? request.fechaInicio() : LocalDate.now());
        rutinaClonada.setDuracionSemanas(rutinaOriginal.getDuracionSemanas());

        // Duplicar cada item de PlanEjercicio
        if (rutinaOriginal.getPlanesEjercicio() != null) {
            for (PlanEjercicio originalPlan : rutinaOriginal.getPlanesEjercicio()) {
                PlanEjercicio clonPlan = new PlanEjercicio();
                clonPlan.setEjercicio(originalPlan.getEjercicio());
                clonPlan.setSesion(originalPlan.getSesion());
                clonPlan.setNumeroSemana(originalPlan.getNumeroSemana());
                clonPlan.setOrden(originalPlan.getOrden());
                clonPlan.setSeries(originalPlan.getSeries());
                clonPlan.setReps(originalPlan.getReps());
                clonPlan.setPausaSegundos(originalPlan.getPausaSegundos());
                clonPlan.setCargaPct(originalPlan.getCargaPct());
                clonPlan.setRpe(originalPlan.getRpe());

                rutinaClonada.addPlanEjercicio(clonPlan);
            }
        }

        return rutinaRepository.save(rutinaClonada);
    }

    @Transactional(readOnly = true)
    public Rutina obtenerPorId(Long id) {
        return rutinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rutina no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Rutina> listarPorAlumno(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));
        return rutinaRepository.findByAlumnoId(alumnoId);
    }

    @Transactional(readOnly = true)
    public List<Rutina> listarPorProfesor(Long profesorId) {
        profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con ID: " + profesorId));
        return rutinaRepository.findByProfesorId(profesorId);
    }
}
