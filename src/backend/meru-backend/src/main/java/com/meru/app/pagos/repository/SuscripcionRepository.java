package com.meru.app.pagos.repository;

import com.meru.app.pagos.domain.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    List<Suscripcion> findByAlumnoId(Long alumnoId);
}
