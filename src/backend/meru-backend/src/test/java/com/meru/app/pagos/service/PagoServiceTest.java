package com.meru.app.pagos.service;

import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.MetodoPago;
import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.dto.PagoResponseDTO;
import com.meru.app.pagos.dto.ProcesarPagoRequestDTO;
import com.meru.app.pagos.event.PagoConfirmadoEvent;
import com.meru.app.pagos.repository.PagoRepository;
import com.meru.app.pagos.repository.SuscripcionRepository;
import com.meru.app.pagos.strategy.EfectivoProcessor;
import com.meru.app.pagos.strategy.MercadoPagoProcessor;
import com.meru.app.pagos.strategy.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        List<PaymentProcessor> processors = List.of(
                new EfectivoProcessor(),
                new MercadoPagoProcessor()
        );
        pagoService = new PagoService(pagoRepository, suscripcionRepository, processors, eventPublisher);
    }

    @Test
    void procesarPago_conEfectivo_debeAprobarInmediatamente_activarSuscripcion_yPublicarEvento() {
        Long suscripcionId = 1L;
        Alumno alumno = new Alumno();
        alumno.setId(10L);

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(suscripcionId);
        suscripcion.setAlumno(alumno);
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);

        when(suscripcionRepository.findById(suscripcionId)).thenReturn(Optional.of(suscripcion));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(500L);
            return p;
        });

        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("15000.00"),
                MetodoPago.EFECTIVO,
                suscripcionId,
                null // ordenId null respeta chk_pago_un_solo_concepto
        );

        PagoResponseDTO response = pagoService.procesarPago(request);

        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals(EstadoPago.APROBADO, response.estado());
        assertEquals(MetodoPago.EFECTIVO, response.metodo());
        assertTrue(response.referenciaExterna().startsWith("REC-EFECTIVO-"));

        // Validar activación de suscripción
        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        verify(suscripcionRepository, times(1)).save(suscripcion);

        // Validar publicación de evento PagoConfirmadoEvent
        ArgumentCaptor<PagoConfirmadoEvent> eventCaptor = ArgumentCaptor.forClass(PagoConfirmadoEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        PagoConfirmadoEvent event = eventCaptor.getValue();
        assertEquals(500L, event.pagoId());
        assertEquals(10L, event.alumnoId());
        assertEquals(new BigDecimal("15000.00"), event.monto());
        assertEquals(suscripcionId, event.suscripcionId());
    }

    @Test
    void procesarPago_conMercadoPago_debeQuedarPendiente_conPreferenciaGenerada_sinActivarSuscripcion() {
        Long suscripcionId = 2L;
        Alumno alumno = new Alumno();
        alumno.setId(20L);

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(suscripcionId);
        suscripcion.setAlumno(alumno);
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);

        when(suscripcionRepository.findById(suscripcionId)).thenReturn(Optional.of(suscripcion));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(600L);
            return p;
        });

        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("20000.00"),
                MetodoPago.MERCADOPAGO,
                suscripcionId,
                null
        );

        PagoResponseDTO response = pagoService.procesarPago(request);

        assertNotNull(response);
        assertEquals(600L, response.id());
        assertEquals(EstadoPago.PENDIENTE, response.estado());
        assertEquals(MetodoPago.MERCADOPAGO, response.metodo());
        assertTrue(response.referenciaExterna().startsWith("MP-PREF-"));

        // No debe activar la suscripción todavía (espera webhook)
        assertEquals(EstadoSuscripcion.VENCIDA, suscripcion.getEstado());
        verify(suscripcionRepository, never()).save(suscripcion);

        // No debe publicar PagoConfirmadoEvent al estar pendiente
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void procesarPago_debeRechazar_cuandoViolaChkPagoUnSoloConcepto_ambosNulos() {
        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("5000.00"),
                MetodoPago.EFECTIVO,
                null,
                null // Ambos nulos viola chk_pago_un_solo_concepto
        );

        assertThrows(IllegalArgumentException.class, () -> pagoService.procesarPago(request));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void procesarPago_debeRechazar_cuandoViolaChkPagoUnSoloConcepto_ambosPresentes() {
        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("5000.00"),
                MetodoPago.EFECTIVO,
                1L,
                2L // Ambos presentes viola chk_pago_un_solo_concepto
        );

        assertThrows(IllegalArgumentException.class, () -> pagoService.procesarPago(request));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void procesarPago_debeRechazar_cuandoSuscripcionNoExiste() {
        when(suscripcionRepository.findById(99L)).thenReturn(Optional.empty());

        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("5000.00"),
                MetodoPago.EFECTIVO,
                99L,
                null
        );

        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.procesarPago(request));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void procesarPago_debeRechazar_cuandoMetodoNoTieneProcessor() {
        // Creamos un servicio sin processors
        PagoService serviceSinProcessors = new PagoService(
                pagoRepository, suscripcionRepository, List.of(), eventPublisher
        );

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));

        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("5000.00"),
                MetodoPago.EFECTIVO,
                1L,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> serviceSinProcessors.procesarPago(request));
    }

    @Test
    void procesarWebhookMercadoPago_debeAprobarPago_activarSuscripcion_yPublicarEvento() {
        Long pagoId = 100L;
        Pago pago = new Pago();
        pago.setId(pagoId);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMonto(new BigDecimal("1000.00"));

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);
        pago.setSuscripcion(suscripcion);

        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        pagoService.procesarWebhookMercadoPago(pagoId, "approved");

        assertEquals(EstadoPago.APROBADO, pago.getEstado());
        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        verify(pagoRepository, times(1)).save(pago);
        verify(suscripcionRepository, times(1)).save(suscripcion);
        verify(eventPublisher, times(1)).publishEvent(any(PagoConfirmadoEvent.class));
    }

    @Test
    void procesarWebhookMercadoPago_idempotencia_noDebeProcesarPagoYaAprobado() {
        Long pagoId = 100L;
        Pago pago = new Pago();
        pago.setId(pagoId);
        pago.setEstado(EstadoPago.APROBADO);

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        pago.setSuscripcion(suscripcion);

        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        pagoService.procesarWebhookMercadoPago(pagoId, "approved");

        // No debe guardar nada nuevo ni publicar evento, porque ya estaba aprobado
        verify(pagoRepository, never()).save(any());
        verify(suscripcionRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
