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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClaseRepository claseRepository;
    private final AlumnoRepository alumnoRepository;
    private final Clock clock;

    public ReservaService(ReservaRepository reservaRepository,
                          ClaseRepository claseRepository,
                          AlumnoRepository alumnoRepository,
                          @Autowired(required = false) Clock clock) {
        this.reservaRepository = reservaRepository;
        this.claseRepository = claseRepository;
        this.alumnoRepository = alumnoRepository;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    @Transactional
    public Reserva reservar(CrearReservaRequestDTO request) {
        Clase clase = claseRepository.findByIdWithLock(request.claseId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase no encontrada con ID: " + request.claseId()));

        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        if (clase.getCupoDisponible() <= 0) {
            throw new CupoAgotadoException("No hay cupo disponible para la clase " + request.claseId());
        }

        if (reservaRepository.existsByClaseIdAndAlumnoIdAndEstado(request.claseId(), request.alumnoId(), EstadoReserva.CONFIRMADA)) {
            throw new ReservaDuplicadaException("El alumno ya posee una reserva confirmada para esta clase");
        }

        // Decrementar cupo
        clase.setCupoDisponible(clase.getCupoDisponible() - 1);
        claseRepository.save(clase);

        Reserva reserva = new Reserva();
        reserva.setClase(clase);
        reserva.setAlumno(alumno);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaReserva(LocalDateTime.now(clock));

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con ID: " + reservaId));

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            return reserva;
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDateTime horarioClase = reserva.getClase().getHorario();

        Duration duracionHastaClase = Duration.between(ahora, horarioClase);
        if (duracionHastaClase.toMinutes() < 30) {
            throw new CancelacionFueraDeTiempoException(
                    "No se puede cancelar una reserva con menos de 30 minutos de antelación al inicio de la clase");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);

        // Incrementar cupo disponible si la clase no ha superado el máximo
        Clase clase = reserva.getClase();
        if (clase.getCupoDisponible() < clase.getCupoMaximo()) {
            clase.setCupoDisponible(clase.getCupoDisponible() + 1);
            claseRepository.save(clase);
        }

        return reservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarPorAlumno(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));
        return reservaRepository.findByAlumnoId(alumnoId);
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarPorClase(Long claseId) {
        claseRepository.findById(claseId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase no encontrada con ID: " + claseId));
        return reservaRepository.findByClaseId(claseId);
    }
}
