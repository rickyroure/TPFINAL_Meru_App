package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.dto.CrearReservaRequestDTO;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.clases.repository.ReservaRepository;
import com.meru.app.common.exception.CancelacionFueraDeTiempoException;
import com.meru.app.common.exception.CupoAgotadoException;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.common.exception.ReservaDuplicadaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClaseRepository claseRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservaService reservaService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(Instant.now());
        lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void reservar_debeTenerExito_decrementandoCupoYGuardandoEstadoConfirmada() {
        Long claseId = 1L;
        Long alumnoId = 2L;

        Clase clase = new Clase();
        clase.setId(claseId);
        clase.setCupoMaximo(10);
        clase.setCupoDisponible(5);
        clase.setHorario(LocalDateTime.now().plusDays(1));

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        when(claseRepository.findByIdWithLock(claseId)).thenReturn(Optional.of(clase));
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(reservaRepository.existsByClaseIdAndAlumnoIdAndEstado(claseId, alumnoId, EstadoReserva.CONFIRMADA))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        Reserva result = reservaService.reservar(new CrearReservaRequestDTO(claseId, alumnoId));

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(EstadoReserva.CONFIRMADA, result.getEstado());
        assertEquals(4, clase.getCupoDisponible(), "El cupo disponible debió decrementarse en 1");
        verify(claseRepository, times(1)).save(clase);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void reservar_debeRechazarse_cuandoCupoDisponibleEsCero() {
        Long claseId = 1L;
        Long alumnoId = 2L;

        Clase clase = new Clase();
        clase.setId(claseId);
        clase.setCupoMaximo(10);
        clase.setCupoDisponible(0);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        when(claseRepository.findByIdWithLock(claseId)).thenReturn(Optional.of(clase));
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));

        assertThrows(CupoAgotadoException.class,
                () -> reservaService.reservar(new CrearReservaRequestDTO(claseId, alumnoId)));
        verify(reservaRepository, never()).save(any());
        verify(claseRepository, never()).save(any());
    }

    @Test
    void reservar_debeRechazarse_cuandoAlumnoYaTieneReservaConfirmadaParaMismaClase() {
        Long claseId = 1L;
        Long alumnoId = 2L;

        Clase clase = new Clase();
        clase.setId(claseId);
        clase.setCupoDisponible(5);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        when(claseRepository.findByIdWithLock(claseId)).thenReturn(Optional.of(clase));
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(reservaRepository.existsByClaseIdAndAlumnoIdAndEstado(claseId, alumnoId, EstadoReserva.CONFIRMADA))
                .thenReturn(true);

        assertThrows(ReservaDuplicadaException.class,
                () -> reservaService.reservar(new CrearReservaRequestDTO(claseId, alumnoId)));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelar_debeTenerExito_cuandoFaltanMasDe30Minutos() {
        // Fijamos la hora actual a las 10:00
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 10, 10, 0, 0);
        // Clase a las 10:31 (faltan 31 minutos)
        LocalDateTime horarioClase = ahora.plusMinutes(31);

        ZoneId zone = ZoneId.systemDefault();
        Instant fixedInstant = ahora.atZone(zone).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zone);

        Clase clase = new Clase();
        clase.setId(1L);
        clase.setHorario(horarioClase);
        clase.setCupoMaximo(10);
        clase.setCupoDisponible(5);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setClase(clase);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva cancelada = reservaService.cancelarReserva(100L);

        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        assertEquals(6, clase.getCupoDisponible(), "El cupo disponible debió incrementarse en 1");
        verify(claseRepository, times(1)).save(clase);
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    void cancelar_debeRechazarse_cuandoFaltanMenosDe30Minutos() {
        // Fijamos la hora actual a las 10:00
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 10, 10, 0, 0);
        // Clase a las 10:29 (faltan 29 minutos)
        LocalDateTime horarioClase = ahora.plusMinutes(29);

        ZoneId zone = ZoneId.systemDefault();
        Instant fixedInstant = ahora.atZone(zone).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zone);

        Clase clase = new Clase();
        clase.setId(1L);
        clase.setHorario(horarioClase);
        clase.setCupoDisponible(5);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setClase(clase);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        assertThrows(CancelacionFueraDeTiempoException.class,
                () -> reservaService.cancelarReserva(100L));
        verify(claseRepository, never()).save(any());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(), "El estado no debió cambiar");
    }
}
