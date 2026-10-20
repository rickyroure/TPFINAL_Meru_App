package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.AfluenciaDiaSemanaDTO;
import com.meru.app.administracion.dto.AfluenciaRangoHorarioDTO;
import com.meru.app.administracion.dto.ReporteAfluenciaResponseDTO;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.clases.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteAfluenciaService {

    private static final List<EstadoReserva> ESTADOS_VALIDOS = List.of(
            EstadoReserva.CONFIRMADA,
            EstadoReserva.ASISTIO
    );

    private static final Map<DayOfWeek, String> NOMBRES_DIA_ES = Map.of(
            DayOfWeek.MONDAY, "LUNES",
            DayOfWeek.TUESDAY, "MARTES",
            DayOfWeek.WEDNESDAY, "MIERCOLES",
            DayOfWeek.THURSDAY, "JUEVES",
            DayOfWeek.FRIDAY, "VIERNES",
            DayOfWeek.SATURDAY, "SABADO",
            DayOfWeek.SUNDAY, "DOMINGO"
    );

    private final ReservaRepository reservaRepository;
    private final CheckinRepository checkinRepository;

    @Transactional(readOnly = true)
    public ReporteAfluenciaResponseDTO obtenerReporteAfluencia() {
        List<Reserva> reservas = reservaRepository.findByEstadoIn(ESTADOS_VALIDOS);
        List<Checkin> checkins = checkinRepository.findAll();

        Map<DayOfWeek, Long> conteoPorDia = new EnumMap<>(DayOfWeek.class);
        Map<Integer, Long> conteoPorHora = new HashMap<>();
        long totalConcurrencias = 0L;

        for (Reserva reserva : reservas) {
            if (reserva.getClase() == null || reserva.getClase().getHorario() == null) {
                continue;
            }
            LocalDateTime fechaHora = reserva.getClase().getHorario();
            conteoPorDia.merge(fechaHora.getDayOfWeek(), 1L, Long::sum);
            conteoPorHora.merge(fechaHora.getHour(), 1L, Long::sum);
            totalConcurrencias++;
        }

        for (Checkin checkin : checkins) {
            if (checkin.getFechaHora() == null) {
                continue;
            }
            LocalDateTime fechaHora = checkin.getFechaHora();
            conteoPorDia.merge(fechaHora.getDayOfWeek(), 1L, Long::sum);
            conteoPorHora.merge(fechaHora.getHour(), 1L, Long::sum);
            totalConcurrencias++;
        }

        if (totalConcurrencias == 0L) {
            return new ReporteAfluenciaResponseDTO(0L, List.of(), List.of());
        }

        List<AfluenciaDiaSemanaDTO> porDiaSemana = conteoPorDia.entrySet().stream()
                .map(entry -> new AfluenciaDiaSemanaDTO(
                        NOMBRES_DIA_ES.get(entry.getKey()),
                        entry.getKey().getValue(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparingLong(AfluenciaDiaSemanaDTO::cantidadConcurrencias).reversed()
                        .thenComparingInt(AfluenciaDiaSemanaDTO::numeroDia))
                .toList();

        List<AfluenciaRangoHorarioDTO> porRangoHorario = conteoPorHora.entrySet().stream()
                .map(entry -> {
                    int hora = entry.getKey();
                    int horaFin = (hora + 1) % 24;
                    String etiquetaRango = String.format("%02d:00 - %02d:00", hora, horaFin);
                    return new AfluenciaRangoHorarioDTO(etiquetaRango, hora, entry.getValue());
                })
                .sorted(Comparator.comparingLong(AfluenciaRangoHorarioDTO::cantidadConcurrencias).reversed()
                        .thenComparingInt(AfluenciaRangoHorarioDTO::horaInicio))
                .toList();

        return new ReporteAfluenciaResponseDTO(totalConcurrencias, porDiaSemana, porRangoHorario);
    }
}
