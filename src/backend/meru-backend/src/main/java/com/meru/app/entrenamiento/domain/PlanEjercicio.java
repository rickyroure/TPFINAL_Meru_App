package com.meru.app.entrenamiento.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_ejercicio")
public class PlanEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Pattern(regexp = "^[A-E]$", message = "La sesión debe ser una letra entre A y E")
    @Column(nullable = false, length = 1)
    private String sesion;

    @Column(name = "numero_semana", nullable = false)
    private Integer numeroSemana;

    @Column(nullable = false)
    private Integer orden = 1;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "pausa_segundos")
    private Integer pausaSegundos;

    @Column(name = "carga_pct", precision = 5, scale = 2)
    private BigDecimal cargaPct;

    @Column(precision = 4, scale = 2)
    private BigDecimal rpe;

    public PlanEjercicio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Rutina getRutina() { return rutina; }
    public void setRutina(Rutina rutina) { this.rutina = rutina; }

    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }

    public String getSesion() { return sesion; }
    public void setSesion(String sesion) {
        if (sesion != null && !sesion.matches("^[A-E]$")) {
            throw new IllegalArgumentException("La sesión debe ser una letra entre A y E");
        }
        this.sesion = sesion;
    }

    public Integer getNumeroSemana() { return numeroSemana; }
    public void setNumeroSemana(Integer numeroSemana) { this.numeroSemana = numeroSemana; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getPausaSegundos() { return pausaSegundos; }
    public void setPausaSegundos(Integer pausaSegundos) { this.pausaSegundos = pausaSegundos; }

    public BigDecimal getCargaPct() { return cargaPct; }
    public void setCargaPct(BigDecimal cargaPct) { this.cargaPct = cargaPct; }

    public BigDecimal getRpe() { return rpe; }
    public void setRpe(BigDecimal rpe) { this.rpe = rpe; }
}
