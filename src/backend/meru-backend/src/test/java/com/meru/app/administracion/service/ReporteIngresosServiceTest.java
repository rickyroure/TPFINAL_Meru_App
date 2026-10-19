package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.IngresoMensualDTO;
import com.meru.app.administracion.dto.ReporteIngresosResponseDTO;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteIngresosServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private ReporteIngresosService reporteIngresosService;

    @Test
    void obtenerReporteIngresos_anual_debeSumarYAgruparPagosAprobadosPorMesYOrigen() {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(1L);

        // Marzo 2026: 1 suscripcion ($25000.00) + 1 orden shop ($12500.50)
        Pago p1 = new Pago();
        p1.setId(1L);
        p1.setSuscripcion(suscripcion);
        p1.setMonto(new BigDecimal("25000.00"));
        p1.setMetodo(MetodoPago.MERCADOPAGO);
        p1.setEstado(EstadoPago.APROBADO);
        p1.setFecha(LocalDateTime.of(2026, 3, 10, 14, 30));

        Pago p2 = new Pago();
        p2.setId(2L);
        p2.setOrdenId(100L);
        p2.setMonto(new BigDecimal("12500.50"));
        p2.setMetodo(MetodoPago.EFECTIVO);
        p2.setEstado(EstadoPago.APROBADO);
        p2.setFecha(LocalDateTime.of(2026, 3, 22, 18, 0));

        // Octubre 2026: 1 suscripcion ($30000.00)
        Pago p3 = new Pago();
        p3.setId(3L);
        p3.setSuscripcion(suscripcion);
        p3.setMonto(new BigDecimal("30000.00"));
        p3.setMetodo(MetodoPago.EFECTIVO);
        p3.setEstado(EstadoPago.APROBADO);
        p3.setFecha(LocalDateTime.of(2026, 10, 5, 10, 15));

        when(pagoRepository.findByEstadoAndFechaBetweenOrderByFechaAsc(
                eq(EstadoPago.APROBADO),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(p1, p2, p3));

        ReporteIngresosResponseDTO reporte = reporteIngresosService.obtenerReporteIngresos(2026, null);

        assertEquals(2026, reporte.anio());
        assertNull(reporte.mes());
        assertEquals(new BigDecimal("67500.50"), reporte.totalIngresos());
        assertEquals(new BigDecimal("55000.00"), reporte.ingresosSuscripciones());
        assertEquals(new BigDecimal("12500.50"), reporte.ingresosShop());
        assertEquals(3L, reporte.cantidadPagos());
        assertEquals(2, reporte.desgloseMensual().size());

        IngresoMensualDTO marzo = reporte.desgloseMensual().get(0);
        assertEquals(2026, marzo.anio());
        assertEquals(3, marzo.mes());
        assertEquals(new BigDecimal("37500.50"), marzo.totalIngresos());
        assertEquals(new BigDecimal("25000.00"), marzo.ingresosSuscripciones());
        assertEquals(new BigDecimal("12500.50"), marzo.ingresosShop());
        assertEquals(2L, marzo.cantidadPagos());

        IngresoMensualDTO octubre = reporte.desgloseMensual().get(1);
        assertEquals(2026, octubre.anio());
        assertEquals(10, octubre.mes());
        assertEquals(new BigDecimal("30000.00"), octubre.totalIngresos());
        assertEquals(new BigDecimal("30000.00"), octubre.ingresosSuscripciones());
        assertEquals(new BigDecimal("0.00"), octubre.ingresosShop());
        assertEquals(1L, octubre.cantidadPagos());
    }

    @Test
    void obtenerReporteIngresos_conMesEspecifico_debeConsultarRangoDelMes() {
        when(pagoRepository.findByEstadoAndFechaBetweenOrderByFechaAsc(
                eq(EstadoPago.APROBADO),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        ReporteIngresosResponseDTO reporte = reporteIngresosService.obtenerReporteIngresos(2026, 10);

        assertEquals(2026, reporte.anio());
        assertEquals(10, reporte.mes());
        assertEquals(new BigDecimal("0.00"), reporte.totalIngresos());
        assertTrue(reporte.desgloseMensual().isEmpty());

        verify(pagoRepository).findByEstadoAndFechaBetweenOrderByFechaAsc(
                eq(EstadoPago.APROBADO),
                eq(LocalDateTime.of(2026, 10, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2026, 10, 31, 23, 59, 59))
        );
    }

    @Test
    void obtenerReporteIngresos_conMesInvalido_debeLanzarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> reporteIngresosService.obtenerReporteIngresos(2026, 13));
        assertThrows(IllegalArgumentException.class,
                () -> reporteIngresosService.obtenerReporteIngresos(2026, 0));
    }
}
