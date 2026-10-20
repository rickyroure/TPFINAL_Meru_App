package com.meru.app.alumnos.controller;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.dto.ActualizarAptoFisicoRequestDTO;
import com.meru.app.alumnos.dto.ActualizarPerfilAlumnoRequestDTO;
import com.meru.app.alumnos.dto.AlumnoRequestDTO;
import com.meru.app.alumnos.dto.AlumnoResponseDTO;
import com.meru.app.alumnos.service.AlumnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumnos")
@Tag(name = "Alumnos", description = "Gestión de alumnos, perfil personal y apto físico")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @PostMapping
    @Operation(summary = "Registrar perfil de alumno vinculado a un usuario")
    public ResponseEntity<AlumnoResponseDTO> registrarAlumno(@Valid @RequestBody AlumnoRequestDTO request) {
        Alumno alumno = alumnoService.registrarAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AlumnoResponseDTO.fromEntity(alumno));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar datos de un alumno por ID")
    public ResponseEntity<AlumnoResponseDTO> obtenerAlumno(@PathVariable Long id) {
        Alumno alumno = alumnoService.obtenerAlumno(id);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }

    @PutMapping("/{id}/profesor/{profesorId}")
    @Operation(summary = "Asignar un profesor referente al alumno")
    public ResponseEntity<AlumnoResponseDTO> asignarProfesor(@PathVariable Long id, @PathVariable Long profesorId) {
        Alumno alumno = alumnoService.asignarProfesor(id, profesorId);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }

    @PutMapping("/{id}/perfil")
    @Operation(summary = "Editar datos personales del perfil del alumno (A1)")
    public ResponseEntity<AlumnoResponseDTO> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPerfilAlumnoRequestDTO request
    ) {
        Alumno alumno = alumnoService.actualizarPerfil(id, request);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }

    @PatchMapping("/{id}/apto-fisico")
    @Operation(summary = "Actualizar estado del apto físico del alumno (A2)")
    public ResponseEntity<AlumnoResponseDTO> actualizarAptoFisico(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAptoFisicoRequestDTO request
    ) {
        Alumno alumno = alumnoService.actualizarAptoFisico(id, request);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }
}

