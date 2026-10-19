package com.meru.app.shop.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.alumnos.repository.AlumnoRepository;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.common.exception.StockInsuficienteException;
import com.meru.app.pagos.event.PagoConfirmadoEvent;
import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.ItemOrden;
import com.meru.app.shop.domain.Orden;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.CrearOrdenRequestDTO;
import com.meru.app.shop.dto.ItemOrdenRequestDTO;
import com.meru.app.shop.repository.OrdenRepository;
import com.meru.app.shop.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private OrdenService ordenService;

    @Test
    void crearOrden_conStockSuficiente_debeCrearOrdenPendienteYCalcularTotalSinDescontarStock() {
        Alumno alumno = new Alumno();
        alumno.setId(10L);

        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("Proteína Whey");
        p1.setPrecio(new BigDecimal("30000.00"));
        p1.setStock(5);

        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setNombre("Agua Mineral");
        p2.setPrecio(new BigDecimal("1500.00"));
        p2.setStock(10);

        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> {
            Orden o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO(
                10L,
                List.of(
                        new ItemOrdenRequestDTO(1L, 1),
                        new ItemOrdenRequestDTO(2L, 2)
                )
        );

        Orden result = ordenService.crearOrden(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(EstadoOrden.PENDIENTE, result.getEstado());
        assertEquals(new BigDecimal("33000.00"), result.getTotal());
        assertEquals(2, result.getItems().size());
        // El stock NO debe descontarse al crear la orden, sino al confirmar el pago
        assertEquals(5, p1.getStock());
        assertEquals(10, p2.getStock());
        verify(productoRepository, never()).save(any());
        verify(ordenRepository, times(1)).save(any(Orden.class));
    }

    @Test
    void crearOrden_conStockInsuficiente_debeRechazarConStockInsuficienteException() {
        Alumno alumno = new Alumno();
        alumno.setId(10L);

        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("Proteína Whey");
        p1.setPrecio(new BigDecimal("30000.00"));
        p1.setStock(1);

        when(alumnoRepository.findById(10L)).thenReturn(Optional.of(alumno));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p1));

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO(
                10L,
                List.of(new ItemOrdenRequestDTO(1L, 3))
        );

        assertThrows(StockInsuficienteException.class,
                () -> ordenService.crearOrden(request));

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void crearOrden_cuandoAlumnoNoExiste_debeLanzarRecursoNoEncontradoException() {
        when(alumnoRepository.findById(99L)).thenReturn(Optional.empty());

        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO(
                99L,
                List.of(new ItemOrdenRequestDTO(1L, 1))
        );

        assertThrows(RecursoNoEncontradoException.class,
                () -> ordenService.crearOrden(request));

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void confirmarPagoOrden_conStockSuficiente_debeDescontarStockYMarcarOrdenPagada() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Creatina");
        producto.setPrecio(new BigDecimal("25000.00"));
        producto.setStock(5);

        Orden orden = new Orden();
        orden.setId(50L);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setTotal(new BigDecimal("50000.00"));

        ItemOrden item = new ItemOrden();
        item.setId(1L);
        item.setOrden(orden);
        item.setProducto(producto);
        item.setCantidad(2);
        item.setPrecioUnitario(new BigDecimal("25000.00"));
        orden.setItems(List.of(item));

        when(ordenRepository.findById(50L)).thenReturn(Optional.of(orden));
        when(productoRepository.findByIdWithLock(1L)).thenReturn(Optional.of(producto));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));

        Orden result = ordenService.confirmarPagoOrden(50L);

        assertEquals(EstadoOrden.PAGADA, result.getEstado());
        assertEquals(3, producto.getStock());
        verify(productoRepository, times(1)).findByIdWithLock(1L);
        verify(productoRepository, times(1)).save(producto);
        verify(ordenRepository, times(1)).save(orden);
    }

    @Test
    void confirmarPagoOrden_siStockSeAgotoEntreCreacionYPago_debeLanzarStockInsuficienteException() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Creatina");
        producto.setPrecio(new BigDecimal("25000.00"));
        producto.setStock(0); // Otro alumno compró la última unidad antes de este pago

        Orden orden = new Orden();
        orden.setId(50L);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setTotal(new BigDecimal("25000.00"));

        ItemOrden item = new ItemOrden();
        item.setId(1L);
        item.setOrden(orden);
        item.setProducto(producto);
        item.setCantidad(1);
        item.setPrecioUnitario(new BigDecimal("25000.00"));
        orden.setItems(List.of(item));

        when(ordenRepository.findById(50L)).thenReturn(Optional.of(orden));
        when(productoRepository.findByIdWithLock(1L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class,
                () -> ordenService.confirmarPagoOrden(50L));

        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        assertEquals(0, producto.getStock());
        verify(productoRepository, never()).save(any());
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void confirmarPagoOrden_siYaEstabaPagada_debeSerIdempotenteYNoDescontarDoble() {
        Orden orden = new Orden();
        orden.setId(50L);
        orden.setEstado(EstadoOrden.PAGADA);

        when(ordenRepository.findById(50L)).thenReturn(Optional.of(orden));

        Orden result = ordenService.confirmarPagoOrden(50L);

        assertEquals(EstadoOrden.PAGADA, result.getEstado());
        verify(productoRepository, never()).findByIdWithLock(any());
        verify(productoRepository, never()).save(any());
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void onPagoConfirmado_cuandoEventoTieneOrdenId_debeConfirmarPagoOrden() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Bebida Isotónica");
        producto.setPrecio(new BigDecimal("2000.00"));
        producto.setStock(10);

        Orden orden = new Orden();
        orden.setId(77L);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setTotal(new BigDecimal("2000.00"));

        ItemOrden item = new ItemOrden();
        item.setId(1L);
        item.setOrden(orden);
        item.setProducto(producto);
        item.setCantidad(1);
        item.setPrecioUnitario(new BigDecimal("2000.00"));
        orden.setItems(List.of(item));

        when(ordenRepository.findById(77L)).thenReturn(Optional.of(orden));
        when(productoRepository.findByIdWithLock(1L)).thenReturn(Optional.of(producto));

        PagoConfirmadoEvent event = new PagoConfirmadoEvent(
                1L, 10L, new BigDecimal("2000.00"), null, 77L
        );

        ordenService.onPagoConfirmado(event);

        assertEquals(EstadoOrden.PAGADA, orden.getEstado());
        assertEquals(9, producto.getStock());
    }
}
