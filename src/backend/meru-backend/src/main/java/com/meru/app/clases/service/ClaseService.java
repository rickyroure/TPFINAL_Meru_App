package com.meru.app.clases.service;

import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.CrearClaseRequestDTO;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final ProfesorRepository profesorRepository;

    public ClaseService(ClaseRepository claseRepository, ProfesorRepository profesorRepository) {
        this.claseRepository = claseRepository;
        this.profesorRepository = profesorRepository;
    }

    @Transactional
    public Clase crearClase(CrearClaseRequestDTO request) {
        Profesor profesor = profesorRepository.findById(request.profesorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con ID: " + request.profesorId()));

        Clase clase = new Clase();
        clase.setProfesor(profesor);
        clase.setTipoActividad(request.tipoActividad());
        clase.setHorario(request.horario());
        clase.setCupoMaximo(request.cupoMaximo());
        clase.setCupoDisponible(request.cupoMaximo());

        return claseRepository.save(clase);
    }

    @Transactional(readOnly = true)
    public List<Clase> listarClases(String tipoActividad, LocalDateTime desde, LocalDateTime hasta) {
        if (tipoActividad != null && !tipoActividad.isBlank() && desde != null && hasta != null) {
            return claseRepository.findByTipoActividadIgnoreCaseAndHorarioBetween(tipoActividad.trim(), desde, hasta);
        } else if (tipoActividad != null && !tipoActividad.isBlank()) {
            return claseRepository.findByTipoActividadIgnoreCase(tipoActividad.trim());
        } else if (desde != null && hasta != null) {
            return claseRepository.findByHorarioBetween(desde, hasta);
        }
        return claseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Clase obtenerPorId(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase no encontrada con ID: " + id));
    }
}
