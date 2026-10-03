package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.ClaseResponseDTO;
import com.meru.app.clases.dto.CrearClaseRequestDTO;
import com.meru.app.clases.service.ClaseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/clases")
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    @PostMapping
    public ResponseEntity<ClaseResponseDTO> crearClase(@Valid @RequestBody CrearClaseRequestDTO request) {
        Clase clase = claseService.crearClase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClaseResponseDTO.fromEntity(clase));
    }

    @GetMapping
    public ResponseEntity<List<ClaseResponseDTO>> listarClases(
            @RequestParam(required = false) String tipoActividad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<ClaseResponseDTO> list = claseService.listarClases(tipoActividad, desde, hasta)
                .stream()
                .map(ClaseResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaseResponseDTO> obtenerPorId(@PathVariable Long id) {
        Clase clase = claseService.obtenerPorId(id);
        return ResponseEntity.ok(ClaseResponseDTO.fromEntity(clase));
    }
}
