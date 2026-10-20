package com.meru.app.profesores.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.dto.ProfesorRequestDTO;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;

    public ProfesorService(ProfesorRepository profesorRepository,
                           UsuarioRepository usuarioRepository,
                           AlumnoRepository alumnoRepository) {
        this.profesorRepository = profesorRepository;
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
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

    @Transactional(readOnly = true)
    public List<Alumno> listarAlumnosAsignados(Long profesorId) {
        obtenerProfesor(profesorId);
        return alumnoRepository.findByProfesorId(profesorId);
    }
}

