package com.meru.app.pagos.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.dto.CrearPlanRequestDTO;
import com.meru.app.pagos.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public Plan crearPlan(CrearPlanRequestDTO request) {
        Plan plan = new Plan();
        plan.setNombre(request.nombre());
        plan.setPrecio(request.precio());
        plan.setDuracionDias(request.duracionDias());
        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<Plan> listarPlanes() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plan obtenerPorId(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con ID: " + id));
    }
}
