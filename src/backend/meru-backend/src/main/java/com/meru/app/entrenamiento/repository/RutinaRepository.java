package com.meru.app.entrenamiento.repository;

import com.meru.app.entrenamiento.domain.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findByAlumnoId(Long alumnoId);
    List<Rutina> findByProfesorId(Long profesorId);
}
