package com.meru.app.pagos.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.dto.CrearSuscripcionRequestDTO;
import com.meru.app.pagos.repository.PlanRepository;
import com.meru.app.pagos.repository.SuscripcionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private SuscripcionService suscripcionService;

    @Test
    void crearSuscripcion_debeCalcularFechaFinCorrectamenteSegunDuracionDiasDelPlan() {
        Long alumnoId = 1L;
        Long planId = 10L;
        LocalDate fechaInicio = LocalDate.of(2026, 5, 1);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        Plan plan = new Plan();
        plan.setId(planId);
        plan.setNombre("Mensual");
        plan.setDuracionDias(30);

        CrearSuscripcionRequestDTO request = new CrearSuscripcionRequestDTO(alumnoId, planId, fechaInicio);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> {
            Suscripcion s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        Suscripcion result = suscripcionService.crearSuscripcion(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(alumnoId, result.getAlumno().getId());
        assertEquals(planId, result.getPlan().getId());
        assertEquals(fechaInicio, result.getFechaInicio());
        // fecha_fin = fechaInicio + duracionDias (2026-05-01 + 30 días = 2026-05-31)
        assertEquals(LocalDate.of(2026, 5, 31), result.getFechaFin());
        assertEquals(EstadoSuscripcion.ACTIVA, result.getEstado());
        verify(suscripcionRepository, times(1)).save(any(Suscripcion.class));
    }

    @Test
    void crearSuscripcion_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        CrearSuscripcionRequestDTO request = new CrearSuscripcionRequestDTO(99L, 1L, null);

        assertThrows(RecursoNoEncontradoException.class, () -> suscripcionService.crearSuscripcion(request));
        verify(suscripcionRepository, never()).save(any());
    }

    @Test
    void crearSuscripcion_debeLanzarExcepcion_cuandoPlanNoExiste() {
        when(alumnoRepository.findById(1L)).thenReturn(Optional.of(new Alumno()));
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        CrearSuscripcionRequestDTO request = new CrearSuscripcionRequestDTO(1L, 99L, null);

        assertThrows(RecursoNoEncontradoException.class, () -> suscripcionService.crearSuscripcion(request));
        verify(suscripcionRepository, never()).save(any());
    }

    @Test
    void listarPorAlumno_debeRetornarSuscripcionesDelAlumno() {
        Long alumnoId = 1L;
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(new Alumno()));

        Suscripcion s = new Suscripcion();
        s.setId(10L);
        when(suscripcionRepository.findByAlumnoId(alumnoId)).thenReturn(List.of(s));

        List<Suscripcion> result = suscripcionService.listarPorAlumno(alumnoId);

        assertEquals(1, result.size());
        verify(suscripcionRepository, times(1)).findByAlumnoId(alumnoId);
    }
}
