package com.meru.app.clases.repository;

import com.meru.app.clases.domain.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByClaseId(Long claseId);
    Optional<Calificacion> findByClaseIdAndAlumnoId(Long claseId, Long alumnoId);
}
