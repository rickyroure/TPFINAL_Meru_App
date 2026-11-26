package com.meru.app.seguridad.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
import com.meru.app.seguridad.dto.UsuarioResponseDTO;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void syncUsuario_debeCrearUsuario_siNoExiste() {
        // Arrange
        SyncUsuarioRequestDTO request = new SyncUsuarioRequestDTO("k-123", "test@test.com", "Juan", "Perez");
        when(usuarioRepository.findByKeycloakId("k-123")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = usuarioService.syncUsuario(request);

        // Assert
        assertNotNull(result);
        assertEquals("k-123", result.getKeycloakId());
        assertEquals("test@test.com", result.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void syncUsuario_debeActualizarUsuario_siYaExiste() {
        // Arrange
        SyncUsuarioRequestDTO request = new SyncUsuarioRequestDTO("k-123", "nuevo@test.com", "Juan Carlos", "Perez");
        Usuario existente = new Usuario();
        existente.setKeycloakId("k-123");
        existente.setEmail("viejo@test.com");
        existente.setNombre("Juan");
        existente.setApellido("Perez");

        when(usuarioRepository.findByKeycloakId("k-123")).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario result = usuarioService.syncUsuario(request);

        // Assert
        assertNotNull(result);
        assertEquals("k-123", result.getKeycloakId());
        assertEquals("nuevo@test.com", result.getEmail());
        assertEquals("Juan Carlos", result.getNombre());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void sincronizarUsuarioYPerfiles_debeRetornarAlumnoIdYProfesorIdExistentes() {
        // Arrange
        SyncUsuarioRequestDTO request = new SyncUsuarioRequestDTO("k-alumno", "alumno@meru.app", "Ana", "Gomez");
        Usuario existente = new Usuario();
        existente.setId(10L);
        existente.setKeycloakId("k-alumno");
        existente.setEmail("alumno@meru.app");
        existente.setNombre("Ana");
        existente.setApellido("Gomez");

        Alumno alumno = new Alumno();
        alumno.setId(3L);
        alumno.setUsuario(existente);

        when(usuarioRepository.findByKeycloakId("k-alumno")).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(existente);
        when(alumnoRepository.findByUsuarioId(10L)).thenReturn(Optional.of(alumno));
        when(profesorRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());

        // Act
        UsuarioResponseDTO response = usuarioService.sincronizarUsuarioYPerfiles(request, true);

        // Assert
        assertEquals(10L, response.id());
        assertEquals(3L, response.alumnoId());
        assertNull(response.profesorId());
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    void sincronizarUsuarioYPerfiles_debeAutoCrearPerfilAlumno_siTieneRolAlumnoYNoExisteFilaEnAlumno() {
        // Arrange
        SyncUsuarioRequestDTO request = new SyncUsuarioRequestDTO("k-nuevo", "nuevo@meru.app", "Lucas", "Diaz");
        Usuario creado = new Usuario();
        creado.setId(15L);
        creado.setKeycloakId("k-nuevo");
        creado.setEmail("nuevo@meru.app");
        creado.setNombre("Lucas");
        creado.setApellido("Diaz");

        when(usuarioRepository.findByKeycloakId("k-nuevo")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(creado);
        when(alumnoRepository.findByUsuarioId(15L)).thenReturn(Optional.empty());
        when(alumnoRepository.save(any(Alumno.class))).thenAnswer(inv -> {
            Alumno a = inv.getArgument(0);
            a.setId(7L);
            return a;
        });
        when(profesorRepository.findByUsuarioId(15L)).thenReturn(Optional.empty());

        // Act
        UsuarioResponseDTO response = usuarioService.sincronizarUsuarioYPerfiles(request, true);

        // Assert
        assertEquals(15L, response.id());
        assertEquals(7L, response.alumnoId());
        assertNull(response.profesorId());
        verify(alumnoRepository, times(1)).save(any(Alumno.class));
    }

    @Test
    void sincronizarUsuarioYPerfiles_debeRetornarProfesorId_siEsProfesor() {
        // Arrange
        SyncUsuarioRequestDTO request = new SyncUsuarioRequestDTO("k-profe", "profe@meru.app", "Carlos", "Gomez");
        Usuario existente = new Usuario();
        existente.setId(20L);
        existente.setKeycloakId("k-profe");
        existente.setEmail("profe@meru.app");
        existente.setNombre("Carlos");
        existente.setApellido("Gomez");

        Profesor profesor = new Profesor();
        profesor.setId(2L);
        profesor.setUsuario(existente);

        when(usuarioRepository.findByKeycloakId("k-profe")).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(existente);
        when(alumnoRepository.findByUsuarioId(20L)).thenReturn(Optional.empty());
        when(profesorRepository.findByUsuarioId(20L)).thenReturn(Optional.of(profesor));

        // Act
        UsuarioResponseDTO response = usuarioService.sincronizarUsuarioYPerfiles(request, false);

        // Assert
        assertEquals(20L, response.id());
        assertNull(response.alumnoId());
        assertEquals(2L, response.profesorId());
    }
}
