package com.meru.app.clases.repository;

import com.meru.app.clases.domain.EstadoReserva;
import com.meru.app.clases.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByClaseIdAndAlumnoIdAndEstado(Long claseId, Long alumnoId, EstadoReserva estado);
    Optional<Reserva> findByClaseIdAndAlumnoIdAndEstado(Long claseId, Long alumnoId, EstadoReserva estado);
    List<Reserva> findByAlumnoId(Long alumnoId);
    List<Reserva> findByClaseId(Long claseId);
}
