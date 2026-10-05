package com.meru.app.pagos.controller;

import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.dto.CrearPlanRequestDTO;
import com.meru.app.pagos.dto.PlanResponseDTO;
import com.meru.app.pagos.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanResponseDTO> crearPlan(@Valid @RequestBody CrearPlanRequestDTO request) {
        Plan plan = planService.crearPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlanResponseDTO.fromEntity(plan));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> listarPlanes() {
        List<PlanResponseDTO> list = planService.listarPlanes()
                .stream()
                .map(PlanResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponseDTO> obtenerPorId(@PathVariable Long id) {
        Plan plan = planService.obtenerPorId(id);
        return ResponseEntity.ok(PlanResponseDTO.fromEntity(plan));
    }
}
