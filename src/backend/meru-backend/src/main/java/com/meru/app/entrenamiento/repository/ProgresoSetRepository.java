package com.meru.app.entrenamiento.repository;

import com.meru.app.entrenamiento.domain.ProgresoSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgresoSetRepository extends JpaRepository<ProgresoSet, Long> {
    List<ProgresoSet> findByPlanEjercicioIdOrderByFechaAscNumeroSerieAsc(Long planEjercicioId);
}
