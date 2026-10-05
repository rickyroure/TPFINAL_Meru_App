package com.meru.app.pagos.repository;

import com.meru.app.pagos.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findBySuscripcionId(Long suscripcionId);
    List<Pago> findByOrdenId(Long ordenId);
}
