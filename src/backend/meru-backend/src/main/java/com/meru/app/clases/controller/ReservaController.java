package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.dto.CrearReservaRequestDTO;
import com.meru.app.clases.dto.ReservaResponseDTO;
import com.meru.app.clases.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> reservar(@Valid @RequestBody CrearReservaRequestDTO request) {
        Reserva reserva = reservaService.reservar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponseDTO.fromEntity(reserva));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
        Reserva reserva = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(ReservaResponseDTO.fromEntity(reserva));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Reserva reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(ReservaResponseDTO.fromEntity(reserva));
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<ReservaResponseDTO> list = reservaService.listarPorAlumno(alumnoId)
                .stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/clase/{claseId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorClase(@PathVariable Long claseId) {
        List<ReservaResponseDTO> list = reservaService.listarPorClase(claseId)
                .stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
