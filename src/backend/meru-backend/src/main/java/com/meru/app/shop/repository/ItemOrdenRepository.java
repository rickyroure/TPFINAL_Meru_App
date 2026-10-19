package com.meru.app.shop.repository;

import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.ItemOrden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {

    @EntityGraph(attributePaths = {"producto", "orden"})
    List<ItemOrden> findByOrdenEstadoAndOrdenFechaBetween(
            EstadoOrden estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @EntityGraph(attributePaths = {"producto", "orden"})
    List<ItemOrden> findByOrdenEstado(EstadoOrden estado);
}
