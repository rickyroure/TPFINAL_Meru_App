package com.meru.app.entrenamiento.controller;

import com.meru.app.entrenamiento.domain.Rutina;
import com.meru.app.entrenamiento.dto.AsignarRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.CrearRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.RutinaResponseDTO;
import com.meru.app.entrenamiento.service.RutinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rutinas")
public class RutinaController {

    private final RutinaService rutinaService;

    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }

    @PostMapping("/plantillas")
    public ResponseEntity<RutinaResponseDTO> crearRutinaPlantilla(@Valid @RequestBody CrearRutinaRequestDTO request) {
        Rutina rutina = rutinaService.crearRutinaPlantilla(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RutinaResponseDTO.fromEntity(rutina));
    }

    @PostMapping("/{id}/asignar")
    public ResponseEntity<RutinaResponseDTO> asignarRutina(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRutinaRequestDTO request) {
        Rutina rutinaClonada = rutinaService.asignarRutina(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RutinaResponseDTO.fromEntity(rutinaClonada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Rutina rutina = rutinaService.obtenerPorId(id);
        return ResponseEntity.ok(RutinaResponseDTO.fromEntity(rutina));
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<RutinaResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<RutinaResponseDTO> list = rutinaService.listarPorAlumno(alumnoId)
                .stream()
                .map(RutinaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<RutinaResponseDTO>> listarPorProfesor(@PathVariable Long profesorId) {
        List<RutinaResponseDTO> list = rutinaService.listarPorProfesor(profesorId)
                .stream()
                .map(RutinaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
