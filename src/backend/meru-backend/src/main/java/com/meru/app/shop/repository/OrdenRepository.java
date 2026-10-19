package com.meru.app.shop.repository;

import com.meru.app.shop.domain.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByAlumnoId(Long alumnoId);
}
