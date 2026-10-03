package com.meru.app.alumnos.controller;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.dto.AlumnoRequestDTO;
import com.meru.app.alumnos.dto.AlumnoResponseDTO;
import com.meru.app.alumnos.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @PostMapping
    public ResponseEntity<AlumnoResponseDTO> registrarAlumno(@Valid @RequestBody AlumnoRequestDTO request) {
        Alumno alumno = alumnoService.registrarAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AlumnoResponseDTO.fromEntity(alumno));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlumnoResponseDTO> obtenerAlumno(@PathVariable Long id) {
        Alumno alumno = alumnoService.obtenerAlumno(id);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }

    @PutMapping("/{id}/profesor/{profesorId}")
    public ResponseEntity<AlumnoResponseDTO> asignarProfesor(@PathVariable Long id, @PathVariable Long profesorId) {
        Alumno alumno = alumnoService.asignarProfesor(id, profesorId);
        return ResponseEntity.ok(AlumnoResponseDTO.fromEntity(alumno));
    }
}
