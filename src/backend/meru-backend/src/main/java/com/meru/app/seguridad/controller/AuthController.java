package com.meru.app.seguridad.controller;

import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
import com.meru.app.seguridad.dto.UsuarioResponseDTO;
import com.meru.app.seguridad.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/sync")
    public ResponseEntity<UsuarioResponseDTO> sync(@Valid @RequestBody SyncUsuarioRequestDTO request) {
        Usuario usuario = usuarioService.syncUsuario(request);
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }
}
