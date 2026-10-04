package com.meru.app.entrenamiento.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Alumno1RM;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.dto.Registrar1RMRequestDTO;
import com.meru.app.entrenamiento.repository.Alumno1RMRepository;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class Alumno1RMService {

    private final Alumno1RMRepository alumno1RMRepository;
    private final AlumnoRepository alumnoRepository;
    private final EjercicioRepository ejercicioRepository;

    public Alumno1RMService(Alumno1RMRepository alumno1RMRepository,
                            AlumnoRepository alumnoRepository,
                            EjercicioRepository ejercicioRepository) {
        this.alumno1RMRepository = alumno1RMRepository;
        this.alumnoRepository = alumnoRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @Transactional
    public Alumno1RM registrar1RM(Long alumnoId, Registrar1RMRequestDTO request) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));

        Ejercicio ejercicio = ejercicioRepository.findById(request.ejercicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado con ID: " + request.ejercicioId()));

        Alumno1RM alumno1RM = new Alumno1RM();
        alumno1RM.setAlumno(alumno);
        alumno1RM.setEjercicio(ejercicio);
        alumno1RM.setValor1rm(request.valor1rm());
        alumno1RM.setFechaMedicion(request.fechaMedicion() != null ? request.fechaMedicion() : LocalDate.now());

        return alumno1RMRepository.save(alumno1RM);
    }

    @Transactional(readOnly = true)
    public List<Alumno1RM> listarHistorico1RM(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));
        return alumno1RMRepository.findByAlumnoIdOrderByFechaMedicionDesc(alumnoId);
    }
}
