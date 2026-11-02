package com.meru.app.seguridad.dto;

import com.meru.app.seguridad.domain.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String keycloakId,
    String email,
    String nombre,
    String apellido,
    String estado,
    Long alumnoId,
    Long profesorId
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return fromEntity(usuario, null, null);
    }

    public static UsuarioResponseDTO fromEntity(Usuario usuario, Long alumnoId, Long profesorId) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getKeycloakId(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEstado() != null ? usuario.getEstado().name() : "ACTIVO",
            alumnoId,
            profesorId
        );
    }
}
