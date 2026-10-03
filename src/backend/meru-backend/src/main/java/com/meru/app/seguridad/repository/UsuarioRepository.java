package com.meru.app.seguridad.repository;

import com.meru.app.seguridad.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByKeycloakId(String keycloakId);
}
