package com.meru.app.alumnos.repository;

import com.meru.app.alumnos.domain.Alumno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    Optional<Alumno> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "profesor", "profesor.usuario"})
    List<Alumno> findByProfesorId(Long profesorId);
}

