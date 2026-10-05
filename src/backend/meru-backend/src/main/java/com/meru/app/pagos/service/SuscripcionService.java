package com.meru.app.pagos.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.dto.CrearSuscripcionRequestDTO;
import com.meru.app.pagos.repository.PlanRepository;
import com.meru.app.pagos.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;
    private final AlumnoRepository alumnoRepository;

    public SuscripcionService(SuscripcionRepository suscripcionRepository,
                              PlanRepository planRepository,
                              AlumnoRepository alumnoRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Transactional
    public Suscripcion crearSuscripcion(CrearSuscripcionRequestDTO request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + request.planId()));

        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(plan.getDuracionDias());

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setAlumno(alumno);
        suscripcion.setPlan(plan);
        suscripcion.setFechaInicio(fechaInicio);
        suscripcion.setFechaFin(fechaFin);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);

        return suscripcionRepository.save(suscripcion);
    }

    @Transactional(readOnly = true)
    public List<Suscripcion> listarPorAlumno(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));
        return suscripcionRepository.findByAlumnoId(alumnoId);
    }

    @Transactional(readOnly = true)
    public Suscripcion obtenerPorId(Long id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + id));
    }
}
