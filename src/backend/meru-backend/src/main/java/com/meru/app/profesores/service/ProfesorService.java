package com.meru.app.profesores.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.dto.ProfesorRequestDTO;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;

    public ProfesorService(ProfesorRepository profesorRepository, UsuarioRepository usuarioRepository) {
        this.profesorRepository = profesorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Profesor registrarProfesor(ProfesorRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + request.usuarioId()));

        if (profesorRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya se encuentra registrado como Profesor");
        }

        Profesor profesor = new Profesor();
        profesor.setUsuario(usuario);
        profesor.setEspecialidad(request.especialidad());

        return profesorRepository.save(profesor);
    }

    @Transactional(readOnly = true)
    public Profesor obtenerProfesor(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con ID: " + id));
    }
}
