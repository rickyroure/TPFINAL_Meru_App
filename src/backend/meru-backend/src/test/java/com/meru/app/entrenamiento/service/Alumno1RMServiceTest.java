package com.meru.app.entrenamiento.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Alumno1RM;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.dto.Registrar1RMRequestDTO;
import com.meru.app.entrenamiento.repository.Alumno1RMRepository;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Alumno1RMServiceTest {

    @Mock
    private Alumno1RMRepository alumno1RMRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private Alumno1RMService alumno1RMService;

    @Test
    void registrar1RM_debeRegistrarYRetornar_cuandoAlumnoYEjercicioExisten() {
        Long alumnoId = 1L;
        Long ejercicioId = 2L;
        BigDecimal valor = new BigDecimal("100.50");
        LocalDate fecha = LocalDate.of(2026, 5, 10);

        Registrar1RMRequestDTO request = new Registrar1RMRequestDTO(ejercicioId, valor, fecha);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setId(ejercicioId);
        ejercicio.setNombre("Press banca");

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(ejercicioRepository.findById(ejercicioId)).thenReturn(Optional.of(ejercicio));

        Alumno1RM saved = new Alumno1RM();
        saved.setId(10L);
        saved.setAlumno(alumno);
        saved.setEjercicio(ejercicio);
        saved.setValor1rm(valor);
        saved.setFechaMedicion(fecha);

        when(alumno1RMRepository.save(any(Alumno1RM.class))).thenReturn(saved);

        Alumno1RM result = alumno1RMService.registrar1RM(alumnoId, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(alumnoId, result.getAlumno().getId());
        assertEquals(ejercicioId, result.getEjercicio().getId());
        assertEquals(valor, result.getValor1rm());
        assertEquals(fecha, result.getFechaMedicion());
        verify(alumno1RMRepository, times(1)).save(any(Alumno1RM.class));
    }

    @Test
    void registrar1RM_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        Long alumnoId = 1L;
        Registrar1RMRequestDTO request = new Registrar1RMRequestDTO(2L, new BigDecimal("80.00"), null);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumno1RMService.registrar1RM(alumnoId, request));
        verify(ejercicioRepository, never()).findById(any());
        verify(alumno1RMRepository, never()).save(any());
    }

    @Test
    void registrar1RM_debeLanzarExcepcion_cuandoEjercicioNoExiste() {
        Long alumnoId = 1L;
        Long ejercicioId = 2L;
        Registrar1RMRequestDTO request = new Registrar1RMRequestDTO(ejercicioId, new BigDecimal("80.00"), null);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(ejercicioRepository.findById(ejercicioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumno1RMService.registrar1RM(alumnoId, request));
        verify(alumno1RMRepository, never()).save(any());
    }

    @Test
    void listarHistorico1RM_debeRetornarListaDeMediciones_cuandoAlumnoExiste() {
        Long alumnoId = 1L;

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setId(2L);
        ejercicio.setNombre("Sentadilla");

        Alumno1RM r1 = new Alumno1RM();
        r1.setId(10L);
        r1.setAlumno(alumno);
        r1.setEjercicio(ejercicio);
        r1.setValor1rm(new BigDecimal("120.00"));
        r1.setFechaMedicion(LocalDate.now());

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(alumno1RMRepository.findByAlumnoIdOrderByFechaMedicionDesc(alumnoId)).thenReturn(List.of(r1));

        List<Alumno1RM> historico = alumno1RMService.listarHistorico1RM(alumnoId);

        assertNotNull(historico);
        assertEquals(1, historico.size());
        assertEquals(new BigDecimal("120.00"), historico.get(0).getValor1rm());
        verify(alumno1RMRepository, times(1)).findByAlumnoIdOrderByFechaMedicionDesc(alumnoId);
    }

    @Test
    void listarHistorico1RM_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        Long alumnoId = 99L;
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> alumno1RMService.listarHistorico1RM(alumnoId));
        verify(alumno1RMRepository, never()).findByAlumnoIdOrderByFechaMedicionDesc(any());
    }
}
