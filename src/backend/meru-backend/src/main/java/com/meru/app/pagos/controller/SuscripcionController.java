package com.meru.app.pagos.controller;

import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.dto.CrearSuscripcionRequestDTO;
import com.meru.app.pagos.dto.SuscripcionResponseDTO;
import com.meru.app.pagos.service.SuscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @PostMapping
    public ResponseEntity<SuscripcionResponseDTO> crearSuscripcion(@Valid @RequestBody CrearSuscripcionRequestDTO request) {
        Suscripcion suscripcion = suscripcionService.crearSuscripcion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuscripcionResponseDTO.fromEntity(suscripcion));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionResponseDTO> obtenerPorId(@PathVariable Long id) {
        Suscripcion suscripcion = suscripcionService.obtenerPorId(id);
        return ResponseEntity.ok(SuscripcionResponseDTO.fromEntity(suscripcion));
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<SuscripcionResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<SuscripcionResponseDTO> list = suscripcionService.listarPorAlumno(alumnoId)
                .stream()
                .map(SuscripcionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
