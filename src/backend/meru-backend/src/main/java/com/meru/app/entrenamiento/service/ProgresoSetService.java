package com.meru.app.entrenamiento.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.PlanEjercicio;
import com.meru.app.entrenamiento.domain.ProgresoSet;
import com.meru.app.entrenamiento.dto.RegistrarProgresoSetRequestDTO;
import com.meru.app.entrenamiento.repository.PlanEjercicioRepository;
import com.meru.app.entrenamiento.repository.ProgresoSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProgresoSetService {

    private final ProgresoSetRepository progresoSetRepository;
    private final PlanEjercicioRepository planEjercicioRepository;

    public ProgresoSetService(ProgresoSetRepository progresoSetRepository,
                              PlanEjercicioRepository planEjercicioRepository) {
        this.progresoSetRepository = progresoSetRepository;
        this.planEjercicioRepository = planEjercicioRepository;
    }

    @Transactional
    public ProgresoSet registrarProgresoSet(RegistrarProgresoSetRequestDTO request) {
        PlanEjercicio plan = planEjercicioRepository.findById(request.planEjercicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de ejercicio no encontrado con ID: " + request.planEjercicioId()));

        ProgresoSet progreso = new ProgresoSet();
        progreso.setPlanEjercicio(plan);
        progreso.setNumeroSerie(request.numeroSerie());
        progreso.setPesoRealizado(request.pesoRealizado());
        progreso.setRepeticionesRealizadas(request.repeticionesRealizadas());
        progreso.setFecha(request.fecha() != null ? request.fecha() : LocalDate.now());

        return progresoSetRepository.save(progreso);
    }

    @Transactional(readOnly = true)
    public List<ProgresoSet> listarPorPlanEjercicio(Long planEjercicioId) {
        planEjercicioRepository.findById(planEjercicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de ejercicio no encontrado con ID: " + planEjercicioId));

        return progresoSetRepository.findByPlanEjercicioIdOrderByFechaAscNumeroSerieAsc(planEjercicioId);
    }
}
