package com.meru.app.clases.repository;

import com.meru.app.clases.domain.Checkin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByAlumnoIdOrderByFechaHoraDesc(Long alumnoId);

    long countByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    @Override
    @EntityGraph(attributePaths = {"alumno"})
    List<Checkin> findAll();
}


