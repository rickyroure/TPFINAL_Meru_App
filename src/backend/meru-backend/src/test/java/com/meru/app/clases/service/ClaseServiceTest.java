package com.meru.app.clases.service;

import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.CrearClaseRequestDTO;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaseServiceTest {

    @Mock
    private ClaseRepository claseRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @InjectMocks
    private ClaseService claseService;

    @Test
    void crearClase_debeCrearClaseConCupoDisponibleIgualACupoMaximo() {
        Long profesorId = 1L;
        Profesor profesor = new Profesor();
        profesor.setId(profesorId);

        LocalDateTime horario = LocalDateTime.now().plusDays(2);
        CrearClaseRequestDTO request = new CrearClaseRequestDTO(profesorId, "Spinning", horario, 20);

        when(profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(claseRepository.save(any(Clase.class))).thenAnswer(inv -> {
            Clase c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        Clase result = claseService.crearClase(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Spinning", result.getTipoActividad());
        assertEquals(20, result.getCupoMaximo());
        assertEquals(20, result.getCupoDisponible(), "El cupo disponible inicial debe ser igual al cupo máximo");
        assertEquals(profesorId, result.getProfesor().getId());
        verify(claseRepository, times(1)).save(any(Clase.class));
    }

    @Test
    void crearClase_debeLanzarExcepcion_cuandoProfesorNoExiste() {
        when(profesorRepository.findById(99L)).thenReturn(Optional.empty());

        CrearClaseRequestDTO request = new CrearClaseRequestDTO(99L, "Yoga", LocalDateTime.now().plusDays(1), 15);

        assertThrows(RecursoNoEncontradoException.class, () -> claseService.crearClase(request));
        verify(claseRepository, never()).save(any());
    }

    @Test
    void listarClases_conFiltros_debeRetornarClasesFiltradas() {
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = desde.plusDays(7);
        Clase c = new Clase();
        c.setId(1L);
        c.setTipoActividad("Crossfit");

        when(claseRepository.findByTipoActividadIgnoreCaseAndHorarioBetween("Crossfit", desde, hasta))
                .thenReturn(List.of(c));

        List<Clase> result = claseService.listarClases("Crossfit", desde, hasta);

        assertEquals(1, result.size());
        assertEquals("Crossfit", result.get(0).getTipoActividad());
    }

    @Test
    void listarClases_sinFiltros_debeRetornarTodas() {
        Clase c = new Clase();
        c.setId(1L);

        when(claseRepository.findAll()).thenReturn(List.of(c));

        List<Clase> result = claseService.listarClases(null, null, null);

        assertEquals(1, result.size());
        verify(claseRepository, times(1)).findAll();
    }
}
