package com.meru.app.shop.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.ProductoRequestDTO;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_debeGuardarYRetornarProducto() {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Proteína Whey 1kg",
                new BigDecimal("35000.00"),
                20
        );

        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Producto result = productoService.crearProducto(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Proteína Whey 1kg", result.getNombre());
        assertEquals(new BigDecimal("35000.00"), result.getPrecio());
        assertEquals(20, result.getStock());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_cuandoExiste_debeModificarPrecioStockYNombre() {
        Producto existente = new Producto();
        existente.setId(1L);
        existente.setNombre("Proteína Whey");
        existente.setPrecio(new BigDecimal("30000.00"));
        existente.setStock(10);

        ProductoRequestDTO request = new ProductoRequestDTO(
                "Proteína Whey Isolada",
                new BigDecimal("42000.00"),
                15
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto result = productoService.actualizarProducto(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Proteína Whey Isolada", result.getNombre());
        assertEquals(new BigDecimal("42000.00"), result.getPrecio());
        assertEquals(15, result.getStock());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(existente);
    }

    @Test
    void actualizarProducto_cuandoNoExiste_debeLanzarRecursoNoEncontradoException() {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Creatina 300g",
                new BigDecimal("28000.00"),
                5
        );

        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.actualizarProducto(99L, request));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_cuandoExiste_debeRetornarProducto() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Bebida Isotónica 500ml");
        producto.setPrecio(new BigDecimal("2500.00"));
        producto.setStock(50);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto result = productoService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Bebida Isotónica 500ml", result.getNombre());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_debeLanzarRecursoNoEncontradoException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.obtenerPorId(99L));
    }

    @Test
    void listarProductos_debeRetornarTodosLosProductos() {
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("Proteína Whey");

        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setNombre("Agua Mineral 500ml");

        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Producto> result = productoService.listarProductos();

        assertEquals(2, result.size());
        verify(productoRepository, times(1)).findAll();
    }
}
