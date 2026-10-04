package com.meru.app.entrenamiento.controller;

import com.meru.app.entrenamiento.domain.ProgresoSet;
import com.meru.app.entrenamiento.dto.ProgresoSetResponseDTO;
import com.meru.app.entrenamiento.dto.RegistrarProgresoSetRequestDTO;
import com.meru.app.entrenamiento.service.ProgresoSetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/progreso-sets")
public class ProgresoSetController {

    private final ProgresoSetService progresoSetService;

    public ProgresoSetController(ProgresoSetService progresoSetService) {
        this.progresoSetService = progresoSetService;
    }

    @PostMapping
    public ResponseEntity<ProgresoSetResponseDTO> registrarProgresoSet(@Valid @RequestBody RegistrarProgresoSetRequestDTO request) {
        ProgresoSet progreso = progresoSetService.registrarProgresoSet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProgresoSetResponseDTO.fromEntity(progreso));
    }

    @GetMapping("/plan-ejercicio/{planEjercicioId}")
    public ResponseEntity<List<ProgresoSetResponseDTO>> listarPorPlanEjercicio(@PathVariable Long planEjercicioId) {
        List<ProgresoSetResponseDTO> list = progresoSetService.listarPorPlanEjercicio(planEjercicioId)
                .stream()
                .map(ProgresoSetResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
