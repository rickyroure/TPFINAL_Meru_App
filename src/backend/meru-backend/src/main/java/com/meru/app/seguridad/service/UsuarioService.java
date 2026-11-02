package com.meru.app.seguridad.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
import com.meru.app.seguridad.dto.UsuarioResponseDTO;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          AlumnoRepository alumnoRepository,
                          ProfesorRepository profesorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.profesorRepository = profesorRepository;
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

    @Transactional
    public UsuarioResponseDTO sincronizarUsuarioYPerfiles(SyncUsuarioRequestDTO request, boolean esRolAlumno) {
        // Si el usuario ya existe en la BD local, preservamos el nombre y apellido que haya editado en su perfil (A1)
        Usuario usuario = usuarioRepository.findByKeycloakId(request.keycloakId())
                .map(existente -> {
                    existente.setEmail(request.email());
                    if (existente.getNombre() == null || existente.getNombre().isBlank()) {
                        existente.setNombre(request.nombre());
                    }
                    if (existente.getApellido() == null || existente.getApellido().isBlank()) {
                        existente.setApellido(request.apellido());
                    }
                    return usuarioRepository.save(existente);
                })
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setKeycloakId(request.keycloakId());
                    nuevo.setEmail(request.email());
                    nuevo.setNombre(request.nombre());
                    nuevo.setApellido(request.apellido());
                    return usuarioRepository.save(nuevo);
                });

        Long alumnoId = alumnoRepository.findByUsuarioId(usuario.getId())
                .map(Alumno::getId)
                .orElseGet(() -> {
                    if (!esRolAlumno) {
                        return null;
                    }
                    Alumno nuevoAlumno = new Alumno();
                    nuevoAlumno.setUsuario(usuario);
                    nuevoAlumno.setAptoFisico(false);
                    nuevoAlumno.setFechaIngreso(LocalDate.now());
                    return alumnoRepository.save(nuevoAlumno).getId();
                });

        Long profesorId = profesorRepository.findByUsuarioId(usuario.getId())
                .map(Profesor::getId)
                .orElse(null);

        return UsuarioResponseDTO.fromEntity(usuario, alumnoId, profesorId);
    }
}
