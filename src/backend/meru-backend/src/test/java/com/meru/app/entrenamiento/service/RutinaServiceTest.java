package com.meru.app.entrenamiento.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.Ejercicio;
import com.meru.app.entrenamiento.domain.PlanEjercicio;
import com.meru.app.entrenamiento.domain.Rutina;
import com.meru.app.entrenamiento.dto.AsignarRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.CrearRutinaRequestDTO;
import com.meru.app.entrenamiento.dto.PlanEjercicioItemDTO;
import com.meru.app.entrenamiento.repository.EjercicioRepository;
import com.meru.app.entrenamiento.repository.RutinaRepository;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutinaServiceTest {

    @Mock
    private RutinaRepository rutinaRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private RutinaService rutinaService;

    @Test
    void crearRutinaPlantilla_debeCrearRutinaConAlumnoIdNullYPlanesEjercicio() {
        Long profesorId = 1L;
        Long ejercicioId = 10L;

        Profesor profesor = new Profesor();
        profesor.setId(profesorId);

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setId(ejercicioId);
        ejercicio.setNombre("Press Banca");

        PlanEjercicioItemDTO itemDTO = new PlanEjercicioItemDTO(
                ejercicioId, "A", 1, 1, 4, 10, 90, null, null
        );

        CrearRutinaRequestDTO request = new CrearRutinaRequestDTO(
                profesorId, "Hipertrofia 4 semanas", 4, List.of(itemDTO)
        );

        when(profesorRepository.findById(profesorId)).thenReturn(Optional.of(profesor));
        when(ejercicioRepository.findById(ejercicioId)).thenReturn(Optional.of(ejercicio));
        when(rutinaRepository.save(any(Rutina.class))).thenAnswer(inv -> {
            Rutina r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        Rutina result = rutinaService.crearRutinaPlantilla(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Hipertrofia 4 semanas", result.getNombre());
        assertNull(result.getAlumno(), "La rutina plantilla debe tener alumno en null");
        assertEquals(profesorId, result.getProfesor().getId());
        assertEquals(1, result.getPlanesEjercicio().size());
        assertEquals("A", result.getPlanesEjercicio().get(0).getSesion());
        verify(rutinaRepository, times(1)).save(any(Rutina.class));
    }

    @Test
    void asignarRutina_debeCrearNuevaRutinaClonadaParaElAlumnoSinModificarLaPlantillaOriginal() {
        Long rutinaPlantillaId = 100L;
        Long alumnoId = 5L;
        LocalDate fechaInicio = LocalDate.of(2026, 6, 1);

        Profesor profesor = new Profesor();
        profesor.setId(1L);

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setId(10L);

        Rutina plantilla = new Rutina();
        plantilla.setId(rutinaPlantillaId);
        plantilla.setNombre("Fuerza 5x5");
        plantilla.setProfesor(profesor);
        plantilla.setAlumno(null); // Plantilla
        plantilla.setDuracionSemanas(6);

        PlanEjercicio plan1 = new PlanEjercicio();
        plan1.setId(1001L);
        plan1.setEjercicio(ejercicio);
        plan1.setSesion("A");
        plan1.setNumeroSemana(1);
        plan1.setOrden(1);
        plan1.setSeries(5);
        plan1.setReps(5);
        plantilla.addPlanEjercicio(plan1);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        AsignarRutinaRequestDTO request = new AsignarRutinaRequestDTO(alumnoId, fechaInicio);

        when(rutinaRepository.findById(rutinaPlantillaId)).thenReturn(Optional.of(plantilla));
        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(alumno));
        when(rutinaRepository.save(any(Rutina.class))).thenAnswer(inv -> {
            Rutina clonada = inv.getArgument(0);
            clonada.setId(200L);
            return clonada;
        });

        Rutina rutinaClonada = rutinaService.asignarRutina(rutinaPlantillaId, request);

        // Verificaciones sobre la rutina clonada
        assertNotNull(rutinaClonada);
        assertEquals(200L, rutinaClonada.getId());
        assertEquals("Fuerza 5x5", rutinaClonada.getNombre());
        assertNotNull(rutinaClonada.getAlumno());
        assertEquals(alumnoId, rutinaClonada.getAlumno().getId());
        assertEquals(fechaInicio, rutinaClonada.getFechaInicio());
        assertEquals(1, rutinaClonada.getPlanesEjercicio().size());

        PlanEjercicio planClonado = rutinaClonada.getPlanesEjercicio().get(0);
        assertNull(planClonado.getId(), "El plan clonado debe ser una nueva entidad sin ID previo");
        assertEquals(rutinaClonada, planClonado.getRutina());
        assertEquals(ejercicio, planClonado.getEjercicio());
        assertEquals("A", planClonado.getSesion());
        assertEquals(5, planClonado.getSeries());

        // Verificación de que la plantilla original permanece intacta
        assertNull(plantilla.getAlumno(), "La plantilla original no debe verse modificada");
        assertEquals(rutinaPlantillaId, plantilla.getId());
        verify(rutinaRepository, times(1)).save(any(Rutina.class));
    }

    @Test
    void asignarRutina_debeLanzarExcepcion_cuandoRutinaNoExiste() {
        when(rutinaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> rutinaService.asignarRutina(99L, new AsignarRutinaRequestDTO(1L, null)));
    }

    @Test
    void asignarRutina_debeLanzarExcepcion_cuandoAlumnoNoExiste() {
        Rutina plantilla = new Rutina();
        plantilla.setId(10L);

        when(rutinaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> rutinaService.asignarRutina(10L, new AsignarRutinaRequestDTO(99L, null)));
    }

    @Test
    void listarPorAlumno_debeRetornarRutinasDelAlumno() {
        Long alumnoId = 1L;
        Rutina r = new Rutina();
        r.setId(10L);

        when(alumnoRepository.findById(alumnoId)).thenReturn(Optional.of(new Alumno()));
        when(rutinaRepository.findByAlumnoId(alumnoId)).thenReturn(List.of(r));

        List<Rutina> list = rutinaService.listarPorAlumno(alumnoId);

        assertEquals(1, list.size());
        verify(rutinaRepository, times(1)).findByAlumnoId(alumnoId);
    }

    @Test
    void listarPorProfesor_debeRetornarRutinasDelProfesor() {
        Long profesorId = 2L;
        Rutina r = new Rutina();
        r.setId(20L);

        when(profesorRepository.findById(profesorId)).thenReturn(Optional.of(new Profesor()));
        when(rutinaRepository.findByProfesorId(profesorId)).thenReturn(List.of(r));

        List<Rutina> list = rutinaService.listarPorProfesor(profesorId);

        assertEquals(1, list.size());
        verify(rutinaRepository, times(1)).findByProfesorId(profesorId);
    }
}
