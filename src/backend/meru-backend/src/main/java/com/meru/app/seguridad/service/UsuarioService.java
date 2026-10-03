package com.meru.app.seguridad.service;

import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario syncUsuario(SyncUsuarioRequestDTO request) {
        return usuarioRepository.findByKeycloakId(request.keycloakId())
                .map(usuario -> {
                    usuario.setEmail(request.email());
                    usuario.setNombre(request.nombre());
                    usuario.setApellido(request.apellido());
                    return usuarioRepository.save(usuario);
                })
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setKeycloakId(request.keycloakId());
                    nuevo.setEmail(request.email());
                    nuevo.setNombre(request.nombre());
                    nuevo.setApellido(request.apellido());
                    return usuarioRepository.save(nuevo);
                });
    }
}
