package com.meru.app.entrenamiento.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.dto.EjercicioRequestDTO;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EjercicioServiceTest {

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private EjercicioService ejercicioService;

    @Test
    void crearEjercicio_debeGuardarYRetornarEjercicio() {
        EjercicioRequestDTO request = new EjercicioRequestDTO("Press de banca", "Pecho", "Press plano con barra");

        Ejercicio saved = new Ejercicio();
        saved.setId(1L);
        saved.setNombre("Press de banca");
        saved.setGrupoMuscular("Pecho");
        saved.setDescripcion("Press plano con barra");

        when(ejercicioRepository.save(any(Ejercicio.class))).thenReturn(saved);

        Ejercicio result = ejercicioService.crearEjercicio(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Press de banca", result.getNombre());
        assertEquals("Pecho", result.getGrupoMuscular());
        assertEquals("Press plano con barra", result.getDescripcion());
        verify(ejercicioRepository, times(1)).save(any(Ejercicio.class));
    }

    @Test
    void listarPorGrupoMuscular_debeRetornarEjerciciosFiltrados() {
        Ejercicio e1 = new Ejercicio();
        e1.setId(1L);
        e1.setNombre("Sentadilla");
        e1.setGrupoMuscular("Piernas");

        when(ejercicioRepository.findByGrupoMuscularIgnoreCase("Piernas")).thenReturn(List.of(e1));

        List<Ejercicio> results = ejercicioService.listarPorGrupoMuscular("Piernas");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Sentadilla", results.get(0).getNombre());
        verify(ejercicioRepository, times(1)).findByGrupoMuscularIgnoreCase("Piernas");
    }

    @Test
    void listarPorGrupoMuscular_cuandoFiltroEsNuloOBlanco_debeRetornarTodos() {
        Ejercicio e1 = new Ejercicio();
        e1.setId(1L);
        e1.setNombre("Sentadilla");

        when(ejercicioRepository.findAll()).thenReturn(List.of(e1));

        List<Ejercicio> results = ejercicioService.listarPorGrupoMuscular(null);

        assertEquals(1, results.size());
        verify(ejercicioRepository, times(1)).findAll();
        verify(ejercicioRepository, never()).findByGrupoMuscularIgnoreCase(anyString());
    }

    @Test
    void obtenerPorId_debeRetornarEjercicio_cuandoExiste() {
        Ejercicio e1 = new Ejercicio();
        e1.setId(1L);
        e1.setNombre("Press militar");

        when(ejercicioRepository.findById(1L)).thenReturn(Optional.of(e1));

        Ejercicio result = ejercicioService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Press militar", result.getNombre());
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        when(ejercicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> ejercicioService.obtenerPorId(99L));
    }
}
