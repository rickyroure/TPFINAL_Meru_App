package com.meru.app.clases.domain;

import com.meru.app.alumnos.domain.Alumno;
import jakarta.persistence.*;

@Entity
@Table(name = "calificacion")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @Column(nullable = false)
    private Integer puntaje;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    public Calificacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Clase getClase() { return clase; }
    public void setClase(Clase clase) { this.clase = clase; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) {
        if (puntaje != null && (puntaje < 1 || puntaje > 5)) {
            throw new IllegalArgumentException("El puntaje debe estar entre 1 y 5");
        }
        this.puntaje = puntaje;
    }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
