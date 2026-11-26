package com.meru.app.profesores.repository;

import com.meru.app.profesores.domain.Profesor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Profesor> findAll();

    Optional<Profesor> findByUsuarioId(Long usuarioId);
}
