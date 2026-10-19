package com.meru.app.shop.controller;

import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.ProductoRequestDTO;
import com.meru.app.shop.dto.ProductoResponseDTO;
import com.meru.app.shop.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@Tag(name = "Shop - Productos", description = "Gestión del catálogo de productos del gimnasio (SH1 / AD3)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Dar de alta un nuevo producto en el shop (solo admin)")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (precio o stock negativo, nombre vacío)")
    @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        Producto producto = productoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductoResponseDTO.fromEntity(producto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Editar nombre, precio y stock de un producto existente (solo admin)")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO request) {
        Producto producto = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(producto));
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos disponibles en el shop")
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos() {
        List<ProductoResponseDTO> list = productoService.listarProductos()
                .stream()
                .map(ProductoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un producto por su ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(producto));
    }
}
