package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Checkin;
import com.meru.app.clases.dto.CheckinRequestDTO;
import com.meru.app.clases.repository.CheckinRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckinService {

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
}
