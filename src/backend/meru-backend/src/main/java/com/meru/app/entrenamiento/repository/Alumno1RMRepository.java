package com.meru.app.entrenamiento.repository;

import com.meru.app.entrenamiento.domain.Alumno1RM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Alumno1RMRepository extends JpaRepository<Alumno1RM, Long> {
    List<Alumno1RM> findByAlumnoIdOrderByFechaMedicionDesc(Long alumnoId);
}
