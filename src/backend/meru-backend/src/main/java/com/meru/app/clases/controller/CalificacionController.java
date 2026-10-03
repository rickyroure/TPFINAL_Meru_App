package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Calificacion;
import com.meru.app.clases.dto.CalificacionRequestDTO;
import com.meru.app.clases.dto.CalificacionResponseDTO;
import com.meru.app.clases.service.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PostMapping
    public ResponseEntity<CalificacionResponseDTO> calificarClase(@Valid @RequestBody CalificacionRequestDTO request) {
        Calificacion calificacion = calificacionService.calificarClase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CalificacionResponseDTO.fromEntity(calificacion));
    }

    @GetMapping("/clase/{claseId}")
    public ResponseEntity<List<CalificacionResponseDTO>> listarPorClase(@PathVariable Long claseId) {
        List<CalificacionResponseDTO> list = calificacionService.listarPorClase(claseId)
                .stream()
                .map(CalificacionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
