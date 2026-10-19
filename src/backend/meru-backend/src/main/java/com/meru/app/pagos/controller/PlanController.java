package com.meru.app.pagos.controller;

import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.dto.CrearPlanRequestDTO;
import com.meru.app.pagos.dto.PlanResponseDTO;
import com.meru.app.pagos.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planes")
@Tag(name = "Planes de Suscripción", description = "Gestión y consulta de planes y tarifas de suscripción")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Crear un nuevo plan de suscripción (Admin)")
    public ResponseEntity<PlanResponseDTO> crearPlan(@Valid @RequestBody CrearPlanRequestDTO request) {
        Plan plan = planService.crearPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlanResponseDTO.fromEntity(plan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Actualizar tarifa o duración de un plan de suscripción existente (AD3)")
    public ResponseEntity<PlanResponseDTO> actualizarPlan(
            @PathVariable Long id,
            @Valid @RequestBody CrearPlanRequestDTO request
    ) {
        Plan plan = planService.actualizarPlan(id, request);
        return ResponseEntity.ok(PlanResponseDTO.fromEntity(plan));
    }

    @GetMapping
    @Operation(summary = "Listar todos los planes de suscripción disponibles (PG1)")
    public ResponseEntity<List<PlanResponseDTO>> listarPlanes() {
        List<PlanResponseDTO> list = planService.listarPlanes()
                .stream()
                .map(PlanResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un plan por ID")
    public ResponseEntity<PlanResponseDTO> obtenerPorId(@PathVariable Long id) {
        Plan plan = planService.obtenerPorId(id);
        return ResponseEntity.ok(PlanResponseDTO.fromEntity(plan));
    }
}

