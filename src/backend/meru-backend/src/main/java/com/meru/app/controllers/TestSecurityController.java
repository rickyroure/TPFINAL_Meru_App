package com.meru.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestSecurityController {

    @GetMapping("/public/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("API publica funcionando");
    }

    @GetMapping("/test/user")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "keycloakId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "claims", jwt.getClaims()
        ));
    }

    @GetMapping("/test/admin-only")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Acceso permitido: Tenes el rol 'admin'");
    }

    @GetMapping("/test/alumno-only")
    @PreAuthorize("hasRole('alumno')")
    public ResponseEntity<String> alumnoOnly() {
        return ResponseEntity.ok("Acceso permitido: Tenes el rol 'alumno'");
    }
}