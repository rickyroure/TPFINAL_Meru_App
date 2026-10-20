package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.dto.CrearReservaRequestDTO;
import com.meru.app.clases.dto.MarcarAsistenciaRequestDTO;
import com.meru.app.clases.dto.ReservaResponseDTO;
import com.meru.app.clases.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@Tag(name = "Reservas de Clases", description = "Reserva de cupos, cancelación con regla de 30 minutos y control de asistencia")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(summary = "Reservar lugar en una clase validando cupo con bloqueo pesimista (C2)")
    public ResponseEntity<ReservaResponseDTO> reservar(@Valid @RequestBody CrearReservaRequestDTO request) {
        Reserva reserva = reservaService.reservar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponseDTO.fromEntity(reserva));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva con al menos 30 minutos de antelación (C3)")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
        Reserva reserva = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(ReservaResponseDTO.fromEntity(reserva));
    }

    @PatchMapping("/{id}/asistencia")
    @Operation(summary = "Marcar asistencia o ausencia del alumno a la clase reservada (C7 y C8)")
    public ResponseEntity<ReservaResponseDTO> marcarAsistencia(
            @PathVariable Long id,
            @Valid @RequestBody MarcarAsistenciaRequestDTO request
    ) {
        Reserva reserva = reservaService.marcarAsistencia(id, Boolean.TRUE.equals(request.asistio()));
        return ResponseEntity.ok(ReservaResponseDTO.fromEntity(reserva));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por ID")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Reserva reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(ReservaResponseDTO.fromEntity(reserva));
    }

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Listar reservas de un alumno")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<ReservaResponseDTO> list = reservaService.listarPorAlumno(alumnoId)
                .stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/clase/{claseId}")
    @Operation(summary = "Listar inscriptos de una clase")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorClase(@PathVariable Long claseId) {
        List<ReservaResponseDTO> list = reservaService.listarPorClase(claseId)
                .stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}

