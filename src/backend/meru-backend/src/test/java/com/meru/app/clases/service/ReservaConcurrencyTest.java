package com.meru.app.clases.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.clases.domain.Clase;
import com.meru.app.clases.dto.CrearReservaRequestDTO;
import com.meru.app.clases.repository.ClaseRepository;
import com.meru.app.clases.repository.ReservaRepository;
import com.meru.app.common.exception.CupoAgotadoException;
import com.meru.app.profesores.domain.Profesor;
import com.meru.app.profesores.repository.ProfesorRepository;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReservaConcurrencyTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    private Clase clase;
    private Profesor profesor;
    private List<Alumno> alumnos;

    @BeforeEach
    void setUp() {

        Usuario usuarioProfesor = new Usuario();
        usuarioProfesor.setKeycloakId("prof-kc-1");
        usuarioProfesor.setEmail("profesor@test.com");
        usuarioProfesor.setNombre("Profesor");
        usuarioProfesor.setApellido("Test");
        usuarioProfesor = usuarioRepository.save(usuarioProfesor);

        profesor = new Profesor();
        profesor.setUsuario(usuarioProfesor);
        profesor.setEspecialidad("Yoga");
        profesor = profesorRepository.save(profesor);

        clase = new Clase();
        clase.setTipoActividad("Yoga");
        clase.setHorario(LocalDateTime.now().plusDays(1));
        clase.setCupoMaximo(1);
        clase.setCupoDisponible(1);
        clase.setProfesor(profesor);
        clase = claseRepository.save(clase);

        alumnos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Usuario usuario = new Usuario();
            usuario.setKeycloakId("test-kc-" + i);
            usuario.setEmail("alumno" + i + "@test.com");
            usuario.setNombre("Alumno " + i);
            usuario.setApellido("Test");
            usuario = usuarioRepository.save(usuario);

            Alumno alumno = new Alumno();
            alumno.setUsuario(usuario);
            alumno.setAptoFisico(true);
            alumno.setFechaIngreso(LocalDate.now());
            alumno = alumnoRepository.save(alumno);
            alumnos.add(alumno);
        }
    }

    @AfterEach
    void tearDown() {
        // Cleaning up created entities
        reservaRepository.deleteAll();
        claseRepository.delete(clase);
        profesorRepository.delete(profesor);
        usuarioRepository.delete(profesor.getUsuario());
        alumnoRepository.deleteAll(alumnos);
        for (Alumno a : alumnos) {
            usuarioRepository.delete(a.getUsuario());
        }
    }

    @Test
    void testReservaConcurrente() throws InterruptedException {
        int threads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger agotadoCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    CrearReservaRequestDTO dto = new CrearReservaRequestDTO(clase.getId(), alumnos.get(index).getId());
                    reservaService.reservar(dto);
                    successCount.incrementAndGet();
                } catch (CupoAgotadoException e) {
                    agotadoCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Solo debe haber 1 reserva exitosa");
        assertEquals(4, agotadoCount.get(), "Debe haber 4 excepciones de cupo agotado");

        Clase claseActualizada = claseRepository.findById(clase.getId()).get();
        assertEquals(0, claseActualizada.getCupoDisponible(), "El cupo disponible no debe ser negativo");
        assertEquals(1, reservaRepository.count(), "Solo debe haber 1 reserva en BD");
    }
}
