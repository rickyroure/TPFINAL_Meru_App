package com.meru.app.entrenamiento.repository;

import com.meru.app.entrenamiento.domain.PlanEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanEjercicioRepository extends JpaRepository<PlanEjercicio, Long> {
    List<PlanEjercicio> findByRutinaIdOrderByNumeroSemanaAscSesionAscOrdenAsc(Long rutinaId);
}
