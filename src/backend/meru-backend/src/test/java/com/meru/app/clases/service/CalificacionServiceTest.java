package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Calificacion;
import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.CalificacionRequestDTO;
import com.meru.app.clases.repository.CalificacionRepository;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private CalificacionRepository calificacionRepository;

    @Mock
    private ClaseRepository claseRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private CalificacionService calificacionService;

    @Test
    void calificarClase_debeGuardarYRetornar_cuandoClaseYAlumnoExistenYPuntajeEsValido() {
        Long claseId = 1L;
        Long alumnoId = 2L;

        Clase clase = new Clase();
        clase.setId(claseId);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        CalificacionRequestDTO request = new CalificacionRequestDTO(claseId, alumnoId, 5, "Excelente clase!");

        when(claseRepository.findById(claseId)).thenReturn(Optional.of(clase));
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));

        Calificacion saved = new Calificacion();
        saved.setId(100L);
        saved.setClase(clase);
        saved.setAlumno(alumno);
        saved.setPuntaje(5);
        saved.setComentario("Excelente clase!");

        when(calificacionRepository.save(any(Calificacion.class))).thenReturn(saved);

        Calificacion result = calificacionService.calificarClase(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(5, result.getPuntaje());
        assertEquals("Excelente clase!", result.getComentario());
        verify(calificacionRepository, times(1)).save(any(Calificacion.class));
    }

    @Test
    void calificarClase_debeLanzarExcepcion_cuandoPuntajeEsInvalido() {
        CalificacionRequestDTO requestBajo = new CalificacionRequestDTO(1L, 2L, 0, "Malo");

        assertThrows(IllegalArgumentException.class, () -> calificacionService.calificarClase(requestBajo));

        CalificacionRequestDTO requestAlto = new CalificacionRequestDTO(1L, 2L, 6, "Excesivo");

        assertThrows(IllegalArgumentException.class, () -> calificacionService.calificarClase(requestAlto));
        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void calificarClase_debeLanzarExcepcion_cuandoClaseNoExiste() {
        when(claseRepository.findById(99L)).thenReturn(Optional.empty());

        CalificacionRequestDTO request = new CalificacionRequestDTO(99L, 2L, 4, null);

        assertThrows(RecursoNoEncontradoException.class, () -> calificacionService.calificarClase(request));
    }
}
