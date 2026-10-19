package com.meru.app.pagos.repository;

import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findBySuscripcionId(Long suscripcionId);
    List<Pago> findByOrdenId(Long ordenId);
    List<Pago> findByEstadoAndFechaBetweenOrderByFechaAsc(EstadoPago estado, LocalDateTime desde, LocalDateTime hasta);
}

