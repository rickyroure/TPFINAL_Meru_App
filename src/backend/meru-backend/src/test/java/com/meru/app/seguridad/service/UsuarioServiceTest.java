package com.meru.app.seguridad.service;

import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.dto.SyncUsuarioRequestDTO;
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
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); // Opcional, depende si queremos actualizar en DB cada vez. Asumiremos que si.
    }
}
