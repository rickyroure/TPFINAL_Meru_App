package com.meru.app.seguridad.dto;

import com.meru.app.seguridad.domain.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String keycloakId,
    String email,
    String nombre,
    String apellido,
    String estado
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getKeycloakId(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEstado().name()
        );
    }
}
