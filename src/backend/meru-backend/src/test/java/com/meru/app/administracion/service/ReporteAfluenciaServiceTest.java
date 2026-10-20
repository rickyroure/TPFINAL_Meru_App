package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.AfluenciaDiaSemanaDTO;
import com.meru.app.administracion.dto.AfluenciaRangoHorarioDTO;
import com.meru.app.administracion.dto.ReporteAfluenciaResponseDTO;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteAfluenciaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @InjectMocks
    private ReporteAfluenciaService reporteAfluenciaService;

    @Test
    void obtenerReporteAfluencia_debeAgruparYOrdenarPorDiaSemanaYRangoHorarioDescendente() {
        // Lunes 19/10/2026 a las 18:00 -> 2 reservas
        Clase claseLunes18 = new Clase();
        claseLunes18.setId(1L);
        claseLunes18.setHorario(LocalDateTime.of(2026, 10, 19, 18, 0));

        Reserva r1 = new Reserva();
        r1.setClase(claseLunes18);
        r1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva r2 = new Reserva();
        r2.setClase(claseLunes18);
        r2.setEstado(EstadoReserva.ASISTIO);

        // Miércoles 21/10/2026 a las 09:00 -> 1 reserva
        Clase claseMiercoles9 = new Clase();
        claseMiercoles9.setId(2L);
        claseMiercoles9.setHorario(LocalDateTime.of(2026, 10, 21, 9, 0));

        Reserva r3 = new Reserva();
        r3.setClase(claseMiercoles9);
        r3.setEstado(EstadoReserva.CONFIRMADA);

        // Check-ins libres:
        // Lunes 19/10/2026 a las 18:30 -> suma a Lunes (total 3) y a franja 18:00 - 19:00 (total 3)
        Checkin c1 = new Checkin();
        c1.setFechaHora(LocalDateTime.of(2026, 10, 19, 18, 30));

        // Miércoles 21/10/2026 a las 18:15 -> suma a Miércoles (total 2) y a franja 18:00 - 19:00 (total 4)
        Checkin c2 = new Checkin();
        c2.setFechaHora(LocalDateTime.of(2026, 10, 21, 18, 15));

        when(reservaRepository.findByEstadoIn(anyCollection())).thenReturn(List.of(r1, r2, r3));
        when(checkinRepository.findAll()).thenReturn(List.of(c1, c2));

        ReporteAfluenciaResponseDTO reporte = reporteAfluenciaService.obtenerReporteAfluencia();

        assertEquals(5L, reporte.totalConcurrencias());

        // Por día de semana: 1° LUNES (3), 2° MIERCOLES (2)
        assertEquals(2, reporte.porDiaSemana().size());
        AfluenciaDiaSemanaDTO dia1 = reporte.porDiaSemana().get(0);
        assertEquals("LUNES", dia1.diaSemana());
        assertEquals(1, dia1.numeroDia());
        assertEquals(3L, dia1.cantidadConcurrencias());

        AfluenciaDiaSemanaDTO dia2 = reporte.porDiaSemana().get(1);
        assertEquals("MIERCOLES", dia2.diaSemana());
        assertEquals(3, dia2.numeroDia());
        assertEquals(2L, dia2.cantidadConcurrencias());

        // Por rango horario: 1° 18:00 - 19:00 (4), 2° 09:00 - 10:00 (1)
        assertEquals(2, reporte.porRangoHorario().size());
        AfluenciaRangoHorarioDTO rango1 = reporte.porRangoHorario().get(0);
        assertEquals("18:00 - 19:00", rango1.rangoHorario());
        assertEquals(18, rango1.horaInicio());
        assertEquals(4L, rango1.cantidadConcurrencias());

        AfluenciaRangoHorarioDTO rango2 = reporte.porRangoHorario().get(1);
        assertEquals("09:00 - 10:00", rango2.rangoHorario());
        assertEquals(9, rango2.horaInicio());
        assertEquals(1L, rango2.cantidadConcurrencias());
    }

    @Test
    void obtenerReporteAfluencia_sinDatos_debeRetornarTotalesEnCeroYListasVacias() {
        when(reservaRepository.findByEstadoIn(anyCollection())).thenReturn(List.of());
        when(checkinRepository.findAll()).thenReturn(List.of());

        ReporteAfluenciaResponseDTO reporte = reporteAfluenciaService.obtenerReporteAfluencia();

        assertEquals(0L, reporte.totalConcurrencias());
        assertTrue(reporte.porDiaSemana().isEmpty());
        assertTrue(reporte.porRangoHorario().isEmpty());
    }
}
