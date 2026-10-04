package com.meru.app.entrenamiento.domain;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.profesores.domain.Profesor;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutina")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "duracion_semanas", nullable = false)
    private Integer duracionSemanas;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroSemana ASC, sesion ASC, orden ASC")
    private List<PlanEjercicio> planesEjercicio = new ArrayList<>();

    public Rutina() {}

    public void addPlanEjercicio(PlanEjercicio item) {
        planesEjercicio.add(item);
        item.setRutina(this);
    }

    public void removePlanEjercicio(PlanEjercicio item) {
        planesEjercicio.remove(item);
        item.setRutina(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public Integer getDuracionSemanas() { return duracionSemanas; }
    public void setDuracionSemanas(Integer duracionSemanas) { this.duracionSemanas = duracionSemanas; }

    public List<PlanEjercicio> getPlanesEjercicio() { return planesEjercicio; }
    public void setPlanesEjercicio(List<PlanEjercicio> planesEjercicio) { this.planesEjercicio = planesEjercicio; }
}
