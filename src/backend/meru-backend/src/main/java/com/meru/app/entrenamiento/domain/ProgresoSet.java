package com.meru.app.entrenamiento.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "progreso_set")
public class ProgresoSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_ejercicio_id", nullable = false)
    private PlanEjercicio planEjercicio;

    @Column(name = "numero_serie", nullable = false)
    private Integer numeroSerie;

    @Column(name = "peso_realizado", nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoRealizado;

    @Column(name = "repeticiones_realizadas")
    private Integer repeticionesRealizadas;

    @Column(nullable = false)
    private LocalDate fecha;

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDate.now();
        }
    }

    public ProgresoSet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanEjercicio getPlanEjercicio() { return planEjercicio; }
    public void setPlanEjercicio(PlanEjercicio planEjercicio) { this.planEjercicio = planEjercicio; }

    public Integer getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(Integer numeroSerie) { this.numeroSerie = numeroSerie; }

    public BigDecimal getPesoRealizado() { return pesoRealizado; }
    public void setPesoRealizado(BigDecimal pesoRealizado) { this.pesoRealizado = pesoRealizado; }

    public Integer getRepeticionesRealizadas() { return repeticionesRealizadas; }
    public void setRepeticionesRealizadas(Integer repeticionesRealizadas) { this.repeticionesRealizadas = repeticionesRealizadas; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
