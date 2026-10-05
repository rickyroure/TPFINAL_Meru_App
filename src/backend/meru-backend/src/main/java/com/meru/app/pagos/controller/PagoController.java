package com.meru.app.pagos.controller;

import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.dto.PagoResponseDTO;
import com.meru.app.pagos.dto.ProcesarPagoRequestDTO;
import com.meru.app.pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponseDTO> procesarPago(@Valid @RequestBody ProcesarPagoRequestDTO request) {
        PagoResponseDTO response = pagoService.procesarPago(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPorId(@PathVariable Long id) {
        Pago pago = pagoService.obtenerPorId(id);
        return ResponseEntity.ok(PagoResponseDTO.fromEntity(pago, null));
    }

    @GetMapping("/suscripcion/{suscripcionId}")
    public ResponseEntity<List<PagoResponseDTO>> listarPorSuscripcion(@PathVariable Long suscripcionId) {
        List<PagoResponseDTO> list = pagoService.listarPorSuscripcion(suscripcionId)
                .stream()
                .map(p -> PagoResponseDTO.fromEntity(p, null))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> webhookMercadoPago(@RequestBody java.util.Map<String, Object> payload) {
        // En una implementación real, extraeríamos el ID del pago o referencia del payload.
        // Simularemos que el payload contiene "pagoId" y "estado" ("approved", "rejected").
        if (payload.containsKey("pagoId") && payload.containsKey("estado")) {
            Long pagoId = Long.valueOf(payload.get("pagoId").toString());
            String estado = payload.get("estado").toString();
            pagoService.procesarWebhookMercadoPago(pagoId, estado);
        }
        return ResponseEntity.ok().build();
    }
}
