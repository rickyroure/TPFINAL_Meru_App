package com.meru.app.clases.repository;

import com.meru.app.clases.domain.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByHorarioBetween(LocalDateTime desde, LocalDateTime hasta);
    List<Clase> findByTipoActividadIgnoreCase(String tipoActividad);
    List<Clase> findByTipoActividadIgnoreCaseAndHorarioBetween(String tipoActividad, LocalDateTime desde, LocalDateTime hasta);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Clase c WHERE c.id = :id")
    java.util.Optional<Clase> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);
}
