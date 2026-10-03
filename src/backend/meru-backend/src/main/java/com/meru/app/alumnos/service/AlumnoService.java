package com.meru.app.alumnos.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.dto.AlumnoRequestDTO;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesorRepository profesorRepository;

    public AlumnoService(AlumnoRepository alumnoRepository,
                         UsuarioRepository usuarioRepository,
                         ProfesorRepository profesorRepository) {
        this.alumnoRepository = alumnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.profesorRepository = profesorRepository;
    }

    @Transactional
    public Alumno registrarAlumno(AlumnoRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + request.usuarioId()));

        if (alumnoRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya se encuentra registrado como Alumno");
        }

        Alumno alumno = new Alumno();
        alumno.setUsuario(usuario);
        alumno.setAptoFisico(request.aptoFisico());
        alumno.setFechaIngreso(LocalDate.now());

        return alumnoRepository.save(alumno);
    }

    @Transactional(readOnly = true)
    public Alumno obtenerAlumno(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + id));
    }

    @Transactional
    public Alumno asignarProfesor(Long alumnoId, Long profesorId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con ID: " + profesorId));

        alumno.setProfesor(profesor);
        return alumnoRepository.save(alumno);
    }
}
