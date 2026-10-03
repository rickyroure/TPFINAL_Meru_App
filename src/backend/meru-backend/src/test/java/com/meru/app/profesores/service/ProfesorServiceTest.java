package com.meru.app.profesores.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.dto.ProfesorRequestDTO;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfesorServiceTest {

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProfesorService profesorService;

    @Test
    void registrarProfesor_debeCrearYRetornarProfesor_cuandoUsuarioExisteYNoTieneProfesor() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombre("Carlos");
        usuario.setApellido("Entrenador");
        usuario.setEmail("carlos@meru.com");

        ProfesorRequestDTO request = new ProfesorRequestDTO(usuarioId, "Musculación");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(profesorRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        Profesor savedProfesor = new Profesor();
        savedProfesor.setId(10L);
        savedProfesor.setUsuario(usuario);
        savedProfesor.setEspecialidad("Musculación");

        when(profesorRepository.save(any(Profesor.class))).thenReturn(savedProfesor);

        Profesor result = profesorService.registrarProfesor(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Musculación", result.getEspecialidad());
        assertEquals(usuarioId, result.getUsuario().getId());
        verify(profesorRepository, times(1)).save(any(Profesor.class));
    }

    @Test
    void registrarProfesor_debeRechazar_cuandoUsuarioNoExiste() {
        Long usuarioId = 1L;
        ProfesorRequestDTO request = new ProfesorRequestDTO(usuarioId, "Crossfit");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> profesorService.registrarProfesor(request));
        verify(profesorRepository, never()).save(any());
    }

    @Test
    void registrarProfesor_debeRechazar_cuandoUsuarioYaEsProfesor() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        Profesor profesorExistente = new Profesor();
        profesorExistente.setId(5L);
        profesorExistente.setUsuario(usuario);

        ProfesorRequestDTO request = new ProfesorRequestDTO(usuarioId, "Spinning");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(profesorRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profesorExistente));

        assertThrows(IllegalArgumentException.class, () -> profesorService.registrarProfesor(request));
        verify(profesorRepository, never()).save(any());
    }

    @Test
    void obtenerProfesor_debeRetornarProfesor_cuandoExiste() {
        Long id = 10L;
        Profesor profesor = new Profesor();
        profesor.setId(id);

        when(profesorRepository.findById(id)).thenReturn(Optional.of(profesor));

        Profesor result = profesorService.obtenerProfesor(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void obtenerProfesor_debeRechazar_cuandoNoExiste() {
        Long id = 10L;
        when(profesorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> profesorService.obtenerProfesor(id));
    }
}
