package com.meru.app.clases.controller;

import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.dto.CheckinRequestDTO;
import com.meru.app.clases.dto.CheckinResponseDTO;
import com.meru.app.clases.service.CheckinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkins")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    public ResponseEntity<CheckinResponseDTO> registrarCheckin(@Valid @RequestBody CheckinRequestDTO request) {
        Checkin checkin = checkinService.registrarCheckin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CheckinResponseDTO.fromEntity(checkin));
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<CheckinResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<CheckinResponseDTO> list = checkinService.listarPorAlumno(alumnoId)
                .stream()
                .map(CheckinResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
