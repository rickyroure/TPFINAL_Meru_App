package com.meru.app.profesores.controller;

import com.meru.app.alumnos.dto.AlumnoResponseDTO;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.dto.ProfesorRequestDTO;
import com.meru.app.profesores.dto.ProfesorResponseDTO;
import com.meru.app.profesores.service.ProfesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profesores")
@Tag(name = "Profesores", description = "Gestión de profesores y consulta de cartera de alumnos asignados")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @PostMapping
    @Operation(summary = "Registrar perfil de profesor vinculado a un usuario")
    public ResponseEntity<ProfesorResponseDTO> registrarProfesor(@Valid @RequestBody ProfesorRequestDTO request) {
        Profesor profesor = profesorService.registrarProfesor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfesorResponseDTO.fromEntity(profesor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil de un profesor por ID")
    public ResponseEntity<ProfesorResponseDTO> obtenerProfesor(@PathVariable Long id) {
        Profesor profesor = profesorService.obtenerProfesor(id);
        return ResponseEntity.ok(ProfesorResponseDTO.fromEntity(profesor));
    }

    @GetMapping("/{id}/alumnos")
    @Operation(summary = "Listar los alumnos asignados al profesor (P1)")
    public ResponseEntity<List<AlumnoResponseDTO>> listarAlumnosAsignados(@PathVariable Long id) {
        List<AlumnoResponseDTO> alumnos = profesorService.listarAlumnosAsignados(id)
                .stream()
                .map(AlumnoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(alumnos);
    }
}

