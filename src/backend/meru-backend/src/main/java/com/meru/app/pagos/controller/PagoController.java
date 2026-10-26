package com.meru.app.pagos.controller;

import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.dto.PagoResponseDTO;
import com.meru.app.pagos.dto.ProcesarPagoRequestDTO;
import com.meru.app.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pagos")
@Tag(name = "Pagos", description = "Procesamiento de pagos de suscripciones y órdenes del shop con MercadoPago o Efectivo (PG2, PG3, SH3)")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @Operation(summary = "Procesar un pago de suscripción o de orden del shop (MercadoPago o Efectivo)")
    @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o concepto de pago ambiguo")
    @ApiResponse(responseCode = "404", description = "Suscripción u orden no encontrada")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente al confirmar el pago de una orden")
    public ResponseEntity<PagoResponseDTO> procesarPago(@Valid @RequestBody ProcesarPagoRequestDTO request) {
        PagoResponseDTO response = pagoService.procesarPago(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por su ID")
    public ResponseEntity<PagoResponseDTO> obtenerPorId(@PathVariable Long id) {
        Pago pago = pagoService.obtenerPorId(id);
        return ResponseEntity.ok(PagoResponseDTO.fromEntity(pago, null));
    }

    @GetMapping("/suscripcion/{suscripcionId}")
    @Operation(summary = "Listar los pagos asociados a una suscripción")
    public ResponseEntity<List<PagoResponseDTO>> listarPorSuscripcion(@PathVariable Long suscripcionId) {
        List<PagoResponseDTO> list = pagoService.listarPorSuscripcion(suscripcionId)
                .stream()
                .map(p -> PagoResponseDTO.fromEntity(p, null))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/orden/{ordenId}")
    @Operation(summary = "Listar los pagos asociados a una orden del shop (SH3)")
    public ResponseEntity<List<PagoResponseDTO>> listarPorOrden(@PathVariable Long ordenId) {
        List<PagoResponseDTO> list = pagoService.listarPorOrden(ordenId)
                .stream()
                .map(p -> PagoResponseDTO.fromEntity(p, null))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/webhook/mercadopago")
    @Operation(summary = "Webhook idempotente de confirmación de pagos de MercadoPago (real y simulado)")
    public ResponseEntity<Void> webhookMercadoPago(
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(name = "id", required = false) String idParam,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "topic", required = false) String topic,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        String resolvedDataId = (dataId != null && !dataId.isBlank()) ? dataId : idParam;
        String resolvedTopic = (type != null && !type.isBlank()) ? type : topic;
        pagoService.procesarNotificacionWebhookMercadoPago(payload, resolvedDataId, resolvedTopic);
        return ResponseEntity.ok().build();
    }
}
