package com.meru.app.pagos.strategy;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoProcessorTest {

    @Mock
    private PreferenceClient preferenceClient;

    @Mock
    private PaymentClient paymentClient;

    @Test
    void procesar_sinAccessToken_debeGenerarPreferenciaSimuladaConCheckoutUrl() {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "",
                preferenceClient,
                paymentClient
        );

        SolicitudPago solicitud = new SolicitudPago(
                new BigDecimal("25000.00"),
                MetodoPago.MERCADOPAGO,
                1L,
                null,
                10L,
                "Meru App - Suscripción Plan Mensual"
        );

        ResultadoPago resultado = processor.procesar(solicitud);

        assertEquals(EstadoPago.PENDIENTE, resultado.estado());
        assertTrue(resultado.referenciaExterna().startsWith("MP-PREF-"));
        assertNotNull(resultado.checkoutUrl());
        assertTrue(resultado.checkoutUrl().contains("sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id="));
        verifyNoInteractions(preferenceClient);
    }

    @Test
    void procesar_conAccessTokenYModoSandbox_debeInvocarPreferenceClientYRetornarSandboxInitPoint() throws Exception {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "TEST-123456789-TOKEN",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "https://meru.ngrok-free.app/api/v1/pagos/webhook/mercadopago",
                preferenceClient,
                paymentClient
        );

        Preference preferenceMock = mock(Preference.class);
        when(preferenceMock.getId()).thenReturn("198273645-pref-id");
        when(preferenceMock.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=198273645-pref-id");
        when(preferenceClient.create(any(PreferenceRequest.class))).thenReturn(preferenceMock);

        SolicitudPago solicitud = new SolicitudPago(
                new BigDecimal("25000.00"),
                MetodoPago.MERCADOPAGO,
                5L,
                null,
                42L,
                "Meru App - Suscripción Plan Libre"
        );

        ResultadoPago resultado = processor.procesar(solicitud);

        assertEquals(EstadoPago.PENDIENTE, resultado.estado());
        assertEquals("198273645-pref-id", resultado.referenciaExterna());
        assertEquals("https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=198273645-pref-id", resultado.checkoutUrl());

        ArgumentCaptor<PreferenceRequest> captor = ArgumentCaptor.forClass(PreferenceRequest.class);
        verify(preferenceClient, times(1)).create(captor.capture());

        PreferenceRequest sentRequest = captor.getValue();
        assertEquals("42", sentRequest.getExternalReference());
        assertEquals("approved", sentRequest.getAutoReturn());
        assertEquals("https://meru.ngrok-free.app/api/v1/pagos/webhook/mercadopago", sentRequest.getNotificationUrl());
        assertEquals(1, sentRequest.getItems().size());
        assertEquals("Meru App - Suscripción Plan Libre", sentRequest.getItems().getFirst().getTitle());
        assertEquals("ARS", sentRequest.getItems().getFirst().getCurrencyId());
        assertEquals(new BigDecimal("25000.00"), sentRequest.getItems().getFirst().getUnitPrice());
    }

    @Test
    void procesar_conAccessTokenYModoProduccion_debeRetornarInitPoint() throws Exception {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "APP_USR-123456789-TOKEN",
                false,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "",
                preferenceClient,
                paymentClient
        );

        Preference preferenceMock = mock(Preference.class);
        when(preferenceMock.getId()).thenReturn("prod-pref-id");
        when(preferenceMock.getInitPoint()).thenReturn("https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=prod-pref-id");
        when(preferenceClient.create(any(PreferenceRequest.class))).thenReturn(preferenceMock);

        SolicitudPago solicitud = new SolicitudPago(
                new BigDecimal("32000.00"),
                MetodoPago.MERCADOPAGO,
                null,
                12L,
                99L,
                "Meru App - Orden Shop #12"
        );

        ResultadoPago resultado = processor.procesar(solicitud);

        assertEquals(EstadoPago.PENDIENTE, resultado.estado());
        assertEquals("prod-pref-id", resultado.referenciaExterna());
        assertEquals("https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=prod-pref-id", resultado.checkoutUrl());
    }

    @Test
    void procesar_cuandoPreferenceClientLanzaMPException_debeLanzarIllegalStateException() throws Exception {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "TEST-123456789-TOKEN",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "",
                preferenceClient,
                paymentClient
        );

        when(preferenceClient.create(any(PreferenceRequest.class))).thenThrow(new MPException("Timeout de red"));

        SolicitudPago solicitud = new SolicitudPago(
                new BigDecimal("15000.00"),
                MetodoPago.MERCADOPAGO,
                1L,
                null,
                10L,
                "Suscripción"
        );

        assertThrows(IllegalStateException.class, () -> processor.procesar(solicitud));
    }

    @Test
    void consultarPagoExterno_conAccessToken_debeInvocarPaymentClientYExtraerExternalReferenceYStatus() throws Exception {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "TEST-123456789-TOKEN",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "",
                preferenceClient,
                paymentClient
        );

        Payment paymentMock = mock(Payment.class);
        when(paymentMock.getExternalReference()).thenReturn("77");
        when(paymentMock.getStatus()).thenReturn("approved");
        when(paymentClient.get(99887766L)).thenReturn(paymentMock);

        MercadoPagoProcessor.DatosPagoExternoMP datos = processor.consultarPagoExterno(99887766L);

        assertNotNull(datos);
        assertEquals(77L, datos.pagoLocalId());
        assertEquals("approved", datos.estadoMp());
        verify(paymentClient, times(1)).get(99887766L);
    }

    @Test
    void consultarPagoExterno_sinAccessToken_debeLanzarIllegalStateException() {
        MercadoPagoProcessor processor = new MercadoPagoProcessor(
                "",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                "",
                preferenceClient,
                paymentClient
        );

        assertThrows(IllegalStateException.class, () -> processor.consultarPagoExterno(99887766L));
        verifyNoInteractions(paymentClient);
    }
}
