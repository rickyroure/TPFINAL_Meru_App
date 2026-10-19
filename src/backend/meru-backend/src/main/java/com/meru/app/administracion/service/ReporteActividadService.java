package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.ActividadComposicionDTO;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.clases.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReporteActividadService {

    private static final List<EstadoReserva> ESTADOS_VALIDOS = List.of(
            EstadoReserva.CONFIRMADA,
            EstadoReserva.ASISTIO
    );

    private final ReservaRepository reservaRepository;
    private final CheckinRepository checkinRepository;

    @Transactional(readOnly = true)
    public List<ActividadComposicionDTO> obtenerComposicionPorActividad() {
        List<Reserva> reservas = reservaRepository.findByEstadoIn(ESTADOS_VALIDOS);
        List<Checkin> checkins = checkinRepository.findAll();

        Map<String, Set<Long>> alumnosPorActividad = new HashMap<>();
        Map<String, Long> participacionesPorActividad = new HashMap<>();

        for (Reserva reserva : reservas) {
            if (reserva.getClase() == null || reserva.getClase().getTipoActividad() == null || reserva.getAlumno() == null) {
                continue;
            }
            String actividad = reserva.getClase().getTipoActividad().trim();
            Long alumnoId = reserva.getAlumno().getId();
            alumnosPorActividad.computeIfAbsent(actividad, k -> new HashSet<>()).add(alumnoId);
            participacionesPorActividad.merge(actividad, 1L, Long::sum);
        }

        for (Checkin checkin : checkins) {
            if (checkin.getActividad() == null || checkin.getAlumno() == null) {
                continue;
            }
            String actividad = checkin.getActividad().trim();
            Long alumnoId = checkin.getAlumno().getId();
            alumnosPorActividad.computeIfAbsent(actividad, k -> new HashSet<>()).add(alumnoId);
            participacionesPorActividad.merge(actividad, 1L, Long::sum);
        }

        if (alumnosPorActividad.isEmpty()) {
            return List.of();
        }

        return alumnosPorActividad.entrySet().stream()
                .map(entry -> {
                    String actividad = entry.getKey();
                    long cantidadAlumnos = entry.getValue().size();
                    long totalParticipaciones = participacionesPorActividad.getOrDefault(actividad, 0L);
                    return new ActividadComposicionDTO(
                            actividad,
                            cantidadAlumnos,
                            totalParticipaciones
                    );
                })
                .sorted(Comparator.comparingLong(ActividadComposicionDTO::cantidadAlumnos).reversed()
                        .thenComparing(ActividadComposicionDTO::tipoActividad))
                .toList();
    }
}
