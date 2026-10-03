package com.meru.app.alumnos.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.dto.AlumnoRequestDTO;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @InjectMocks
    private AlumnoService alumnoService;

    @Test
    void registrarAlumno_debeCrearYRetornarAlumno_cuandoUsuarioExisteYNoTieneAlumno() {
        // Arrange
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setEmail("test@test.com");

        AlumnoRequestDTO request = new AlumnoRequestDTO(usuarioId, true);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(alumnoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        Alumno savedAlumno = new Alumno();
        savedAlumno.setId(100L);
        savedAlumno.setUsuario(usuario);
        savedAlumno.setAptoFisico(true);
        savedAlumno.setFechaIngreso(LocalDate.now());

        when(alumnoRepository.save(any(Alumno.class))).thenReturn(savedAlumno);

        // Act
        Alumno result = alumnoService.registrarAlumno(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(usuarioId, result.getUsuario().getId());
        assertTrue(result.isAptoFisico());
        assertNotNull(result.getFechaIngreso());
        verify(alumnoRepository, times(1)).save(any(Alumno.class));
    }

    @Test
    void registrarAlumno_debeRechazar_cuandoUsuarioNoExiste() {
        Long usuarioId = 1L;
        AlumnoRequestDTO request = new AlumnoRequestDTO(usuarioId, true);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumnoService.registrarAlumno(request));
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    void registrarAlumno_debeRechazar_cuandoUsuarioYaEsAlumno() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Alumno alumnoExistente = new Alumno();
        alumnoExistente.setId(100L);
        alumnoExistente.setUsuario(usuario);

        AlumnoRequestDTO request = new AlumnoRequestDTO(usuarioId, true);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(alumnoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(alumnoExistente));

        assertThrows(IllegalArgumentException.class, () -> alumnoService.registrarAlumno(request));
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    void obtenerAlumno_debeRetornarAlumno_cuandoExiste() {
        Long id = 100L;
        Alumno alumno = new Alumno();
        alumno.setId(id);

        when(alumnoRepository.findById(id)).thenReturn(Optional.of(alumno));

        Alumno result = alumnoService.obtenerAlumno(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void obtenerAlumno_debeRechazar_cuandoNoExiste() {
        Long id = 100L;
        when(alumnoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumnoService.obtenerAlumno(id));
    }

    @Test
    void asignarProfesor_debeAsignarProfesor_cuandoAmbosExisten() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        Profesor profesor = new Profesor();
        profesor.setId(profesorId);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(alumnoRepository.save(any(Alumno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Alumno resultado = alumnoService.asignarProfesor(alumnoId, profesorId);

        assertNotNull(resultado);
        assertNotNull(resultado.getProfesor());
        assertEquals(profesorId, resultado.getProfesor().getId());
        verify(alumnoRepository, times(1)).save(alumno);
    }

    @Test
    void asignarProfesor_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumnoService.asignarProfesor(alumnoId, profesorId));
        verify(profesorRepository, never()).findById(any());
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    void asignarProfesor_debeLanzarExcepcion_cuandoProfesorNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(profesorRepository.findById(profesorId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumnoService.asignarProfesor(alumnoId, profesorId));
        verify(alumnoRepository, never()).save(any());
    }
}
