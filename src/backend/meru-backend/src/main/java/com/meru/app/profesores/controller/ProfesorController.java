package com.meru.app.profesores.controller;

import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.dto.ProfesorRequestDTO;
import com.meru.app.profesores.dto.ProfesorResponseDTO;
import com.meru.app.profesores.service.ProfesorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profesores")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @PostMapping
    public ResponseEntity<ProfesorResponseDTO> registrarProfesor(@Valid @RequestBody ProfesorRequestDTO request) {
        Profesor profesor = profesorService.registrarProfesor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfesorResponseDTO.fromEntity(profesor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesorResponseDTO> obtenerProfesor(@PathVariable Long id) {
        Profesor profesor = profesorService.obtenerProfesor(id);
        return ResponseEntity.ok(ProfesorResponseDTO.fromEntity(profesor));
    }
}
