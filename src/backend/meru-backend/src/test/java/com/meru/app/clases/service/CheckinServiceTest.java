package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.dto.CheckinRequestDTO;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private CheckinService checkinService;

    @Test
    void registrarCheckin_debeGuardarYRetornar_cuandoAlumnoExiste() {
        Long alumnoId = 1L;
        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        LocalDateTime fechaHora = LocalDateTime.now();
        CheckinRequestDTO request = new CheckinRequestDTO(alumnoId, "Musculación", fechaHora);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));

        Checkin saved = new Checkin();
        saved.setId(10L);
        saved.setAlumno(alumno);
        saved.setActividad("Musculación");
        saved.setFechaHora(fechaHora);

        when(checkinRepository.save(any(Checkin.class))).thenReturn(saved);

        Checkin result = checkinService.registrarCheckin(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Musculación", result.getActividad());
        assertEquals(alumnoId, result.getAlumno().getId());
        verify(checkinRepository, times(1)).save(any(Checkin.class));
    }

    @Test
    void registrarCheckin_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        Long alumnoId = 99L;
        CheckinRequestDTO request = new CheckinRequestDTO(alumnoId, "Crossfit", null);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> checkinService.registrarCheckin(request));
        verify(checkinRepository, never()).save(any());
    }
}
