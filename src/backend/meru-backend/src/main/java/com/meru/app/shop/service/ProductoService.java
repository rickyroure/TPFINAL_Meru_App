package com.meru.app.shop.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.ProductoRequestDTO;
import com.meru.app.shop.repository.ProductoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
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
}
