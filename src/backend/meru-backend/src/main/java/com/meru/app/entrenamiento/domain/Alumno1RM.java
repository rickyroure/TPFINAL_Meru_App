package com.meru.app.entrenamiento.domain;

import com.meru.app.alumnos.domain.Alumno;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "alumno_1rm")
public class Alumno1RM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(name = "valor_1rm", nullable = false, precision = 6, scale = 2)
    private BigDecimal valor1rm;

    @Column(name = "fecha_medicion", nullable = false)
    private LocalDate fechaMedicion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaMedicion == null) {
            this.fechaMedicion = LocalDate.now();
        }
    }

    public Alumno1RM() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }

    public BigDecimal getValor1rm() { return valor1rm; }
    public void setValor1rm(BigDecimal valor1rm) { this.valor1rm = valor1rm; }

    public LocalDate getFechaMedicion() { return fechaMedicion; }
    public void setFechaMedicion(LocalDate fechaMedicion) { this.fechaMedicion = fechaMedicion; }
}
