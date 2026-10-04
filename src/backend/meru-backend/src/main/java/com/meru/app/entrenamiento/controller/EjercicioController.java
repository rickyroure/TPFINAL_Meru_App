package com.meru.app.entrenamiento.controller;

import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.dto.EjercicioRequestDTO;
import com.meru.app.entrenamiento.dto.EjercicioResponseDTO;
import com.meru.app.entrenamiento.service.EjercicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @PostMapping
    public ResponseEntity<EjercicioResponseDTO> crearEjercicio(@Valid @RequestBody EjercicioRequestDTO request) {
        Ejercicio ejercicio = ejercicioService.crearEjercicio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EjercicioResponseDTO.fromEntity(ejercicio));
    }

    @GetMapping
    public ResponseEntity<List<EjercicioResponseDTO>> listarEjercicios(
            @RequestParam(required = false) String grupoMuscular) {
        List<EjercicioResponseDTO> list = ejercicioService.listarPorGrupoMuscular(grupoMuscular)
                .stream()
                .map(EjercicioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EjercicioResponseDTO> obtenerPorId(@PathVariable Long id) {
        Ejercicio ejercicio = ejercicioService.obtenerPorId(id);
        return ResponseEntity.ok(EjercicioResponseDTO.fromEntity(ejercicio));
    }
}
