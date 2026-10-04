package com.meru.app.entrenamiento.controller;

import com.meru.app.entrenamiento.domain.Alumno1RM;
import com.meru.app.entrenamiento.dto.Alumno1RMResponseDTO;
import com.meru.app.entrenamiento.dto.Registrar1RMRequestDTO;
import com.meru.app.entrenamiento.service.Alumno1RMService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos/{alumnoId}/1rm")
public class Alumno1RMController {

    private final Alumno1RMService alumno1RMService;

    public Alumno1RMController(Alumno1RMService alumno1RMService) {
        this.alumno1RMService = alumno1RMService;
    }

    @PostMapping
    public ResponseEntity<Alumno1RMResponseDTO> registrar1RM(
            @PathVariable Long alumnoId,
            @Valid @RequestBody Registrar1RMRequestDTO request) {
        Alumno1RM alumno1RM = alumno1RMService.registrar1RM(alumnoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Alumno1RMResponseDTO.fromEntity(alumno1RM));
    }

    @GetMapping
    public ResponseEntity<List<Alumno1RMResponseDTO>> listarHistorico1RM(@PathVariable Long alumnoId) {
        List<Alumno1RMResponseDTO> list = alumno1RMService.listarHistorico1RM(alumnoId)
                .stream()
                .map(Alumno1RMResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
