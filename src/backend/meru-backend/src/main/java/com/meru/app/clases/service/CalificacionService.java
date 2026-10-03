package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Calificacion;
import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.CalificacionRequestDTO;
import com.meru.app.clases.repository.CalificacionRepository;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final ClaseRepository claseRepository;
    private final AlumnoRepository alumnoRepository;

    public CalificacionService(CalificacionRepository calificacionRepository,
                               ClaseRepository claseRepository,
                               AlumnoRepository alumnoRepository) {
        this.calificacionRepository = calificacionRepository;
        this.claseRepository = claseRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Transactional
    public Calificacion calificarClase(CalificacionRequestDTO request) {
        if (request.puntaje() == null || request.puntaje() < 1 || request.puntaje() > 5) {
            throw new IllegalArgumentException("El puntaje debe estar comprendido entre 1 y 5");
        }

        Clase clase = claseRepository.findById(request.claseId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase no encontrada con ID: " + request.claseId()));

        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        Calificacion calificacion = calificacionRepository.findByClaseIdAndAlumnoId(request.claseId(), request.alumnoId())
                .orElseGet(Calificacion::new);

        calificacion.setClase(clase);
        calificacion.setAlumno(alumno);
        calificacion.setPuntaje(request.puntaje());
        calificacion.setComentario(request.comentario());

        return calificacionRepository.save(calificacion);
    }

    @Transactional(readOnly = true)
    public List<Calificacion> listarPorClase(Long claseId) {
        claseRepository.findById(claseId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase no encontrada con ID: " + claseId));
        return calificacionRepository.findByClaseId(claseId);
    }
}
