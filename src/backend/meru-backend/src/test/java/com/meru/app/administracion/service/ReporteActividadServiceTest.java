package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.ActividadComposicionDTO;
import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.clases.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteActividadServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @InjectMocks
    private ReporteActividadService reporteActividadService;

    @Test
    void obtenerComposicionPorActividad_debeAgregarReservasYCheckinsContandoAlumnosUnicosYParticipaciones() {
        Alumno alumno1 = new Alumno();
        alumno1.setId(1L);

        Alumno alumno2 = new Alumno();
        alumno2.setId(2L);

        Alumno alumno3 = new Alumno();
        alumno3.setId(3L);

        Clase claseCrossfit = new Clase();
        claseCrossfit.setId(10L);
        claseCrossfit.setTipoActividad("CrossFit");

        Clase claseYoga = new Clase();
        claseYoga.setId(20L);
        claseYoga.setTipoActividad("Yoga");

        // Alumno 1 tiene 2 reservas en CrossFit y 1 en Yoga (participa en múltiples actividades)
        Reserva r1 = new Reserva();
        r1.setAlumno(alumno1);
        r1.setClase(claseCrossfit);
        r1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva r2 = new Reserva();
        r2.setAlumno(alumno1);
        r2.setClase(claseCrossfit);
        r2.setEstado(EstadoReserva.ASISTIO);

        Reserva r3 = new Reserva();
        r3.setAlumno(alumno1);
        r3.setClase(claseYoga);
        r3.setEstado(EstadoReserva.CONFIRMADA);

        // Alumno 2 tiene 1 reserva en CrossFit
        Reserva r4 = new Reserva();
        r4.setAlumno(alumno2);
        r4.setClase(claseCrossfit);
        r4.setEstado(EstadoReserva.ASISTIO);

        // Check-ins libres: Alumno 2 y Alumno 3 hacen Musculación; Alumno 3 también hace CrossFit
        Checkin c1 = new Checkin();
        c1.setAlumno(alumno2);
        c1.setActividad("Musculación");

        Checkin c2 = new Checkin();
        c2.setAlumno(alumno3);
        c2.setActividad("Musculación");

        Checkin c3 = new Checkin();
        c3.setAlumno(alumno3);
        c3.setActividad("CrossFit");

        when(reservaRepository.findByEstadoIn(anyCollection())).thenReturn(List.of(r1, r2, r3, r4));
        when(checkinRepository.findAll()).thenReturn(List.of(c1, c2, c3));

        List<ActividadComposicionDTO> resultado = reporteActividadService.obtenerComposicionPorActividad();

        assertEquals(3, resultado.size());

        // 1° CrossFit: Alumnos únicos {1, 2, 3} = 3 alumnos, 4 participaciones (r1, r2, r4, c3)
        ActividadComposicionDTO crossfit = resultado.get(0);
        assertEquals("CrossFit", crossfit.tipoActividad());
        assertEquals(3L, crossfit.cantidadAlumnos());
        assertEquals(4L, crossfit.totalParticipaciones());

        // 2° Musculación: Alumnos únicos {2, 3} = 2 alumnos, 2 participaciones (c1, c2)
        ActividadComposicionDTO musculacion = resultado.get(1);
        assertEquals("Musculación", musculacion.tipoActividad());
        assertEquals(2L, musculacion.cantidadAlumnos());
        assertEquals(2L, musculacion.totalParticipaciones());

        // 3° Yoga: Alumnos únicos {1} = 1 alumno, 1 participación (r3)
        ActividadComposicionDTO yoga = resultado.get(2);
        assertEquals("Yoga", yoga.tipoActividad());
        assertEquals(1L, yoga.cantidadAlumnos());
        assertEquals(1L, yoga.totalParticipaciones());
    }

    @Test
    void obtenerComposicionPorActividad_cuandoNoHayDatos_debeRetornarListaVacia() {
        when(reservaRepository.findByEstadoIn(anyCollection())).thenReturn(List.of());
        when(checkinRepository.findAll()).thenReturn(List.of());

        List<ActividadComposicionDTO> resultado = reporteActividadService.obtenerComposicionPorActividad();

        assertTrue(resultado.isEmpty());
    }
}
