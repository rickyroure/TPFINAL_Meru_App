package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.dto.CheckinRequestDTO;
import com.meru.app.clases.dto.CheckinResponseDTO;
import com.meru.app.clases.dto.OcupacionActualResponseDTO;
import com.meru.app.clases.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkins")
@Tag(name = "Check-in y Ocupación", description = "Registro de ingreso libre al gimnasio y consulta de nivel de ocupación actual")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    @Operation(summary = "Registrar ingreso libre al gimnasio indicando actividad (C4)")
    public ResponseEntity<CheckinResponseDTO> registrarCheckin(@Valid @RequestBody CheckinRequestDTO request) {
        Checkin checkin = checkinService.registrarCheckin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CheckinResponseDTO.fromEntity(checkin));
    }

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Listar historial de check-ins de un alumno")
    public ResponseEntity<List<CheckinResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<CheckinResponseDTO> list = checkinService.listarPorAlumno(alumnoId)
                .stream()
                .map(CheckinResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/ocupacion-actual")
    @Operation(summary = "Consultar el nivel de ocupación actual del gimnasio (C5)")
    public ResponseEntity<OcupacionActualResponseDTO> obtenerOcupacionActual() {
        return ResponseEntity.ok(checkinService.obtenerOcupacionActual());
    }
}

