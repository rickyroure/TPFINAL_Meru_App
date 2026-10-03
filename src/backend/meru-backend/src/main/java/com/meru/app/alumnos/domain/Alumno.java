package com.meru.app.alumnos.domain;

import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.profesores.domain.Profesor;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "alumno")
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @Column(name = "apto_fisico", nullable = false)
    private boolean aptoFisico;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public boolean isAptoFisico() { return aptoFisico; }
    public void setAptoFisico(boolean aptoFisico) { this.aptoFisico = aptoFisico; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
}
