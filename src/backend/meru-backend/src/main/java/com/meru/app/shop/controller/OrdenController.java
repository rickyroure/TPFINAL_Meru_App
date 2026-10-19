package com.meru.app.shop.controller;

import com.meru.app.shop.domain.Orden;
import com.meru.app.shop.dto.CrearOrdenRequestDTO;
import com.meru.app.shop.dto.OrdenResponseDTO;
import com.meru.app.shop.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes")
@Tag(name = "Shop - Órdenes", description = "Compra de productos y gestión de órdenes del shop (SH2)")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva orden de compra en el shop")
    @ApiResponse(responseCode = "201", description = "Orden creada en estado PENDIENTE")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (lista vacía o cantidades <= 0)")
    @ApiResponse(responseCode = "404", description = "Alumno o producto no encontrado")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente para alguno de los productos")
    public ResponseEntity<OrdenResponseDTO> crearOrden(@Valid @RequestBody CrearOrdenRequestDTO request) {
        Orden orden = ordenService.crearOrden(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenResponseDTO.fromEntity(orden));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una orden por su ID")
    @ApiResponse(responseCode = "200", description = "Orden encontrada")
    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    public ResponseEntity<OrdenResponseDTO> obtenerPorId(@PathVariable Long id) {
        Orden orden = ordenService.obtenerPorId(id);
        return ResponseEntity.ok(OrdenResponseDTO.fromEntity(orden));
    }

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Listar todas las órdenes de un alumno")
    public ResponseEntity<List<OrdenResponseDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<OrdenResponseDTO> list = ordenService.listarPorAlumno(alumnoId)
                .stream()
                .map(OrdenResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}
