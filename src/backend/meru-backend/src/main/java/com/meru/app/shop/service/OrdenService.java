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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final AlumnoRepository alumnoRepository;

    public OrdenService(OrdenRepository ordenRepository,
                        ProductoRepository productoRepository,
                        AlumnoRepository alumnoRepository) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Transactional
    public Orden crearOrden(CrearOrdenRequestDTO request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + request.alumnoId()));

        Orden orden = new Orden();
        orden.setAlumno(alumno);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setFecha(LocalDateTime.now());

        List<ItemOrden> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemOrdenRequestDTO itemReq : request.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + itemReq.productoId()));

            if (producto.getStock() < itemReq.cantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto '" + producto.getNombre() +
                                "'. Disponible: " + producto.getStock() + ", solicitado: " + itemReq.cantidad()
                );
            }

            ItemOrden item = new ItemOrden();
            item.setOrden(orden);
            item.setProducto(producto);
            item.setCantidad(itemReq.cantidad());
            item.setPrecioUnitario(producto.getPrecio());
            items.add(item);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemReq.cantidad()));
            total = total.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(total);

        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden confirmarPagoOrden(Long ordenId) {
        Orden orden = obtenerPorId(ordenId);

        if (orden.getEstado() == EstadoOrden.PAGADA) {
            return orden;
        }

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new IllegalArgumentException("No se puede confirmar el pago de una orden cancelada");
        }

        // Ordenar por ID de producto al adquirir locks pesimistas para prevenir deadlocks
        List<ItemOrden> itemsOrdenados = orden.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProducto().getId()))
                .toList();

        for (ItemOrden item : itemsOrdenados) {
            Long productoId = item.getProducto().getId();
            Producto producto = productoRepository.findByIdWithLock(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + productoId));

            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente al confirmar el pago para el producto '" + producto.getNombre() +
                                "'. Disponible: " + producto.getStock() + ", requerido: " + item.getCantidad()
                );
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        orden.setEstado(EstadoOrden.PAGADA);
        return ordenRepository.save(orden);
    }

    @EventListener
    @Transactional
    public void onPagoConfirmado(PagoConfirmadoEvent event) {
        if (event.ordenId() != null) {
            confirmarPagoOrden(event.ordenId());
        }
    }

    @Transactional(readOnly = true)
    public Orden obtenerPorId(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Orden> listarPorAlumno(Long alumnoId) {
        return ordenRepository.findByAlumnoId(alumnoId);
    }
}
