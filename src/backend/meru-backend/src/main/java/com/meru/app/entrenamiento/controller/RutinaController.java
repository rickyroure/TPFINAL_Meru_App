package com.meru.app.entrenamiento.controller;

import com.meru.app.entrenamiento.domain.Rutina;
import com.meru.app.entrenamiento.dto.AsignarRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.CrearRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.RutinaResponseDTO;
import com.meru.app.entrenamiento.service.RutinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rutinas")
@Tag(name = "Rutinas y Planes de Entrenamiento", description = "Gestión de rutinas plantilla, asignación por clonación y exportación CSV")
public class RutinaController {

    private final RutinaService rutinaService;

    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }

    @PostMapping("/plantillas")
    @Operation(summary = "Crear una rutina plantilla con sus semanas, sesiones y ejercicios (E2 y E3)")
    public ResponseEntity<RutinaResponseDTO> crearRutinaPlantilla(@Valid @RequestBody CrearRutinaRequestDTO request) {
        Rutina rutina = rutinaService.crearRutinaPlantilla(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RutinaResponseDTO.fromEntity(rutina));
    }

    @PostMapping("/{id}/asignar")
    @Operation(summary = "Asignar rutina a un alumno clonando su estructura para independizarla (E4)")
    public ResponseEntity<RutinaResponseDTO> asignarRutina(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRutinaRequestDTO request) {
        Rutina rutinaClonada = rutinaService.asignarRutina(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RutinaResponseDTO.fromEntity(rutinaClonada));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rutina por ID con el detalle de sus ejercicios")
    public ResponseEntity<RutinaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Rutina rutina = rutinaService.obtenerPorId(id);
        return ResponseEntity.ok(RutinaResponseDTO.fromEntity(rutina));
    }

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Listar rutinas asignadas a un alumno")
    public ResponseEntity<List<RutinaResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<RutinaResponseDTO> list = rutinaService.listarPorAlumno(alumnoId)
                .stream()
                .map(RutinaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/profesor/{profesorId}")
    @Operation(summary = "Listar rutinas creadas por un profesor")
    public ResponseEntity<List<RutinaResponseDTO>> listarPorProfesor(@PathVariable Long profesorId) {
        List<RutinaResponseDTO> list = rutinaService.listarPorProfesor(profesorId)
                .stream()
                .map(RutinaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping(value = "/{id}/exportar-csv", produces = "text/csv")
    @Operation(summary = "Exportar rutina de entrenamiento en formato CSV (E8)")
    public ResponseEntity<String> exportarRutinaCsv(@PathVariable Long id) {
        String csv = rutinaService.exportarRutinaCsv(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rutina-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}

