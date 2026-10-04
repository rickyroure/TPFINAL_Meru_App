package com.meru.app.entrenamiento.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.dto.EjercicioRequestDTO;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    @Transactional
    public Ejercicio crearEjercicio(EjercicioRequestDTO request) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setNombre(request.nombre());
        ejercicio.setGrupoMuscular(request.grupoMuscular());
        ejercicio.setDescripcion(request.descripcion());
        return ejercicioRepository.save(ejercicio);
    }

    @Transactional(readOnly = true)
    public List<Ejercicio> listarPorGrupoMuscular(String grupoMuscular) {
        if (grupoMuscular != null && !grupoMuscular.trim().isEmpty()) {
            return ejercicioRepository.findByGrupoMuscularIgnoreCase(grupoMuscular.trim());
        }
        return ejercicioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ejercicio obtenerPorId(Long id) {
        return ejercicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado con ID: " + id));
    }
}
