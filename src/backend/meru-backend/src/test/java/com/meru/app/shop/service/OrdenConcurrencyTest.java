package com.meru.app.shop.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.StockInsuficienteException;
import com.meru.app.seguridad.domain.Usuario;
import com.meru.app.seguridad.repository.UsuarioRepository;
import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.Orden;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.CrearOrdenRequestDTO;
import com.meru.app.shop.dto.ItemOrdenRequestDTO;
import com.meru.app.shop.repository.OrdenRepository;
import com.meru.app.shop.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class OrdenConcurrencyTest {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Producto productoUltimoStock;
    private List<Alumno> alumnos;
    private List<Orden> ordenesPendientes;

    @BeforeEach
    void setUp() {
        productoUltimoStock = new Producto();
        productoUltimoStock.setNombre("Creatina Monohidratada Edición Limitada");
        productoUltimoStock.setPrecio(new BigDecimal("28000.00"));
        productoUltimoStock.setStock(1);
        productoUltimoStock = productoRepository.save(productoUltimoStock);

        alumnos = new ArrayList<>();
        ordenesPendientes = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Usuario u = new Usuario();
            u.setKeycloakId("shop-kc-" + i);
            u.setEmail("shop.alumno" + i + "@test.com");
            u.setNombre("AlumnoShop " + i);
            u.setApellido("Test");
            u = usuarioRepository.save(u);

            Alumno a = new Alumno();
            a.setUsuario(u);
            a.setAptoFisico(true);
            a.setFechaIngreso(LocalDate.now());
            a = alumnoRepository.save(a);
            alumnos.add(a);

            // Los 5 alumnos arman su carrito/orden cuando todavía había 1 unidad en stock
            CrearOrdenRequestDTO req = new CrearOrdenRequestDTO(
                    a.getId(),
                    List.of(new ItemOrdenRequestDTO(productoUltimoStock.getId(), 1))
            );
            Orden orden = ordenService.crearOrden(req);
            ordenesPendientes.add(orden);
        }
    }

    @AfterEach
    void tearDown() {
        ordenRepository.deleteAll(ordenesPendientes);
        productoRepository.delete(productoUltimoStock);
        alumnoRepository.deleteAll(alumnos);
        for (Alumno a : alumnos) {
            usuarioRepository.delete(a.getUsuario());
        }
    }

    @Test
    void confirmarPagoSimultaneo_sobreUltimoProductoEnStock_debeAprobarSoloUnaOrdenYEvitarSobreventa() throws InterruptedException {
        int threads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger pagadasCount = new AtomicInteger(0);
        AtomicInteger sinStockCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final Long ordenId = ordenesPendientes.get(i).getId();
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    ordenService.confirmarPagoOrden(ordenId);
                    pagadasCount.incrementAndGet();
                } catch (StockInsuficienteException e) {
                    sinStockCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(1, pagadasCount.get(), "Exactamente 1 orden debe poder descontar la última unidad y quedar PAGADA");
        assertEquals(4, sinStockCount.get(), "Las otras 4 órdenes concurrentes deben rechazarse con StockInsuficienteException");

        Producto productoActualizado = productoRepository.findById(productoUltimoStock.getId()).orElseThrow();
        assertEquals(0, productoActualizado.getStock(), "El stock final debe quedar en 0 y nunca negativo");

        long ordenesPagadasEnBd = ordenesPendientes.stream()
                .map(o -> ordenRepository.findById(o.getId()).orElseThrow())
                .filter(o -> o.getEstado() == EstadoOrden.PAGADA)
                .count();
        assertEquals(1, ordenesPagadasEnBd, "En la base de datos solo 1 orden debe figurar en estado PAGADA");
    }
}
