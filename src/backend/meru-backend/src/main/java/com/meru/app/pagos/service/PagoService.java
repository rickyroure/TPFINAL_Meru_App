package com.meru.app.pagos.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.dto.PagoResponseDTO;
import com.meru.app.pagos.dto.ProcesarPagoRequestDTO;
import com.meru.app.pagos.event.PagoConfirmadoEvent;
import com.meru.app.pagos.repository.PagoRepository;
import com.meru.app.pagos.repository.SuscripcionRepository;
import com.meru.app.pagos.strategy.PaymentProcessor;
import com.meru.app.pagos.strategy.ResultadoPago;
import com.meru.app.pagos.strategy.SolicitudPago;
import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.Orden;
import com.meru.app.shop.repository.OrdenRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final OrdenRepository ordenRepository;
    private final List<PaymentProcessor> processors;
    private final ApplicationEventPublisher eventPublisher;

    public PagoService(PagoRepository pagoRepository,
                       SuscripcionRepository suscripcionRepository,
                       OrdenRepository ordenRepository,
                       List<PaymentProcessor> processors,
                       ApplicationEventPublisher eventPublisher) {
        this.pagoRepository = pagoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.ordenRepository = ordenRepository;
        this.processors = processors;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PagoResponseDTO procesarPago(ProcesarPagoRequestDTO request) {
        // Enforzar regla de concepto único (chk_pago_un_solo_concepto)
        boolean hasSuscripcion = request.suscripcionId() != null;
        boolean hasOrden = request.ordenId() != null;
        if ((hasSuscripcion && hasOrden) || (!hasSuscripcion && !hasOrden)) {
            throw new IllegalArgumentException("El pago debe asociarse exclusivamente a una suscripción o a una orden");
        }

        Suscripcion suscripcion = null;
        Orden orden = null;

        if (request.suscripcionId() != null) {
            suscripcion = suscripcionRepository.findById(request.suscripcionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada con ID: " + request.suscripcionId()));
        } else {
            orden = ordenRepository.findById(request.ordenId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con ID: " + request.ordenId()));
            if (orden.getEstado() != EstadoOrden.PENDIENTE) {
                throw new IllegalArgumentException("La orden con ID " + request.ordenId() + " no se encuentra en estado PENDIENTE");
            }
        }

        PaymentProcessor processor = processors.stream()
                .filter(p -> p.metodoSoportado() == request.metodo())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no soportado: " + request.metodo()));

        SolicitudPago solicitud = new SolicitudPago(
                request.monto(),
                request.metodo(),
                request.suscripcionId(),
                request.ordenId()
        );

        ResultadoPago resultado = processor.procesar(solicitud);

        Pago pago = new Pago();
        pago.setSuscripcion(suscripcion);
        pago.setOrdenId(request.ordenId());
        pago.setMonto(request.monto());
        pago.setMetodo(request.metodo());
        pago.setEstado(resultado.estado());
        pago.setFecha(LocalDateTime.now());
        pago.setReferenciaExterna(resultado.referenciaExterna());

        pago = pagoRepository.save(pago);

        if (resultado.estado() == EstadoPago.APROBADO) {
            if (suscripcion != null) {
                suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
                suscripcionRepository.save(suscripcion);
            }

            Long alumnoId = resolverAlumnoId(suscripcion, orden);

            eventPublisher.publishEvent(new PagoConfirmadoEvent(
                    pago.getId(),
                    alumnoId,
                    pago.getMonto(),
                    request.suscripcionId(),
                    request.ordenId()
            ));
        }

        return PagoResponseDTO.fromEntity(pago, resultado.mensaje());
    }

    @Transactional(readOnly = true)
    public List<Pago> listarPorSuscripcion(Long suscripcionId) {
        return pagoRepository.findBySuscripcionId(suscripcionId);
    }

    @Transactional(readOnly = true)
    public List<Pago> listarPorOrden(Long ordenId) {
        return pagoRepository.findByOrdenId(ordenId);
    }

    @Transactional(readOnly = true)
    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con ID: " + id));
    }

    @Transactional
    public void procesarWebhookMercadoPago(Long pagoId, String estadoMp) {
        Pago pago = obtenerPorId(pagoId);

        if (pago.getEstado() == EstadoPago.APROBADO) {
            // Idempotencia estricta: ya está APROBADO, no hacer nada
            return;
        }

        if ("approved".equalsIgnoreCase(estadoMp)) {
            pago.setEstado(EstadoPago.APROBADO);
            pagoRepository.save(pago);

            Suscripcion suscripcion = pago.getSuscripcion();
            if (suscripcion != null) {
                suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
                suscripcionRepository.save(suscripcion);
            }

            Orden orden = pago.getOrdenId() != null
                    ? ordenRepository.findById(pago.getOrdenId()).orElse(null)
                    : null;

            Long alumnoId = resolverAlumnoId(suscripcion, orden);

            eventPublisher.publishEvent(new PagoConfirmadoEvent(
                    pago.getId(),
                    alumnoId,
                    pago.getMonto(),
                    suscripcion != null ? suscripcion.getId() : null,
                    pago.getOrdenId()
            ));
        } else if ("rejected".equalsIgnoreCase(estadoMp)) {
            pago.setEstado(EstadoPago.RECHAZADO);
            pagoRepository.save(pago);
        }
    }

    private Long resolverAlumnoId(Suscripcion suscripcion, Orden orden) {
        if (suscripcion != null && suscripcion.getAlumno() != null) {
            return suscripcion.getAlumno().getId();
        }
        if (orden != null && orden.getAlumno() != null) {
            return orden.getAlumno().getId();
        }
        return null;
    }
}
