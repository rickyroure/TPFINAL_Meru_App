package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.dto.CheckinRequestDTO;
import com.meru.app.clases.dto.OcupacionActualResponseDTO;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckinService {

    private static final int CAPACIDAD_MAXIMA_REFERENCIA = 50;
    private static final int VENTANA_MINUTOS_OCUPACION = 90;

    private final CheckinRepository checkinRepository;
    private final AlumnoRepository alumnoRepository;

    public CheckinService(CheckinRepository checkinRepository, AlumnoRepository alumnoRepository) {
        this.checkinRepository = checkinRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Transactional
    public Checkin registrarCheckin(CheckinRequestDTO request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        Checkin checkin = new Checkin();
        checkin.setAlumno(alumno);
        checkin.setActividad(request.actividad());
        checkin.setFechaHora(request.fechaHora() != null ? request.fechaHora() : LocalDateTime.now());

        return checkinRepository.save(checkin);
    }

    @Transactional(readOnly = true)
    public List<Checkin> listarPorAlumno(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));
        return checkinRepository.findByAlumnoIdOrderByFechaHoraDesc(alumnoId);
    }

    @Transactional(readOnly = true)
    public OcupacionActualResponseDTO obtenerOcupacionActual() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desde = ahora.minusMinutes(VENTANA_MINUTOS_OCUPACION);
        long presentes = checkinRepository.countByFechaHoraBetween(desde, ahora);

        BigDecimal porcentaje = BigDecimal.valueOf(presentes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(CAPACIDAD_MAXIMA_REFERENCIA), 2, RoundingMode.HALF_UP);

        String nivel;
        if (porcentaje.compareTo(new BigDecimal("40.00")) < 0) {
            nivel = "BAJA";
        } else if (porcentaje.compareTo(new BigDecimal("75.00")) <= 0) {
            nivel = "MEDIA";
        } else {
            nivel = "ALTA";
        }

        return new OcupacionActualResponseDTO(presentes, CAPACIDAD_MAXIMA_REFERENCIA, porcentaje, nivel);
    }
}

