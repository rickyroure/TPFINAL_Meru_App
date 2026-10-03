package com.meru.app.seguridad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SyncUsuarioRequestDTO(
    @NotBlank String keycloakId,
    @NotBlank @Email String email,
    @NotBlank String nombre,
    @NotBlank String apellido
) {}
