package com.meru.app.seguridad.controller;

import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
import com.meru.app.seguridad.dto.UsuarioResponseDTO;
import com.meru.app.seguridad.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Sincronización de identidad de Keycloak con base de datos local")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar usuario autenticado desde Keycloak y resolver IDs de perfil (S2)")
    public ResponseEntity<UsuarioResponseDTO> sync(
            @Valid @RequestBody SyncUsuarioRequestDTO request,
            Authentication authentication
    ) {
        boolean esRolAlumno = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ALUMNO".equalsIgnoreCase(a.getAuthority()));
        return ResponseEntity.ok(usuarioService.sincronizarUsuarioYPerfiles(request, esRolAlumno));
    }
}
