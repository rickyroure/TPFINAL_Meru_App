package com.meru.app.pagos.repository;

import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Suscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    List<Suscripcion> findByAlumnoId(Long alumnoId);

    @EntityGraph(attributePaths = {"alumno", "plan"})
    List<Suscripcion> findByEstadoAndFechaFinBetween(EstadoSuscripcion estado, LocalDate desde, LocalDate hasta);
}

