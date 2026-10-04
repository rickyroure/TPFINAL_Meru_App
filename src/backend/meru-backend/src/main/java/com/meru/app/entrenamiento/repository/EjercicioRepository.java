package com.meru.app.entrenamiento.repository;

import com.meru.app.entrenamiento.domain.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
    List<Ejercicio> findByGrupoMuscularIgnoreCase(String grupoMuscular);
}
