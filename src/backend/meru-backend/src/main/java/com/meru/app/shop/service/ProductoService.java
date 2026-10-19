package com.meru.app.shop.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.ItemOrden;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.ProductoMasVendidoDTO;
import com.meru.app.shop.dto.ProductoRequestDTO;
import com.meru.app.shop.repository.ItemOrdenRepository;
import com.meru.app.shop.repository.ProductoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ItemOrdenRepository itemOrdenRepository;

    public ProductoService(ProductoRepository productoRepository,
                           ItemOrdenRepository itemOrdenRepository) {
        this.productoRepository = productoRepository;
        this.itemOrdenRepository = itemOrdenRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('admin')")
    public Producto crearProducto(ProductoRequestDTO request) {
        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        return productoRepository.save(producto);
    }

    @Transactional
    @PreAuthorize("hasRole('admin')")
    public Producto actualizarProducto(Long id, ProductoRequestDTO request) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('admin')")
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDateTime desde, LocalDateTime hasta) {
        List<ItemOrden> itemsPagados = (desde != null && hasta != null)
                ? itemOrdenRepository.findByOrdenEstadoAndOrdenFechaBetween(EstadoOrden.PAGADA, desde, hasta)
                : itemOrdenRepository.findByOrdenEstado(EstadoOrden.PAGADA);

        Map<Long, List<ItemOrden>> agrupadosPorProducto = itemsPagados.stream()
                .collect(Collectors.groupingBy(i -> i.getProducto().getId()));

        return agrupadosPorProducto.values().stream()
                .map(items -> {
                    Producto prod = items.getFirst().getProducto();
                    long cantidadTotal = items.stream()
                            .mapToLong(ItemOrden::getCantidad)
                            .sum();
                    BigDecimal totalRecaudado = items.stream()
                            .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new ProductoMasVendidoDTO(
                            prod.getId(),
                            prod.getNombre(),
                            prod.getStock(),
                            cantidadTotal,
                            totalRecaudado
                    );
                })
                .sorted(Comparator.comparing(ProductoMasVendidoDTO::cantidadVendida).reversed())
                .toList();
    }
}
