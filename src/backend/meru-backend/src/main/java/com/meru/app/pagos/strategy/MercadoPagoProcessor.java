package com.meru.app.pagos.strategy;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MercadoPagoProcessor implements PaymentProcessor {

    public record DatosPagoExternoMP(Long pagoLocalId, String estadoMp) {}

    private final String accessToken;
    private final boolean sandbox;
    private final String backUrlSuccess;
    private final String backUrlFailure;
    private final String backUrlPending;
    private final String notificationUrl;
    private final PreferenceClient preferenceClient;
    private final PaymentClient paymentClient;

    public MercadoPagoProcessor() {
        this(
                "",
                true,
                "http://localhost:5173/pagos/exitoso",
                "http://localhost:5173/pagos/fallido",
                "http://localhost:5173/pagos/pendiente",
                ""
        );
    }

    @Autowired
    public MercadoPagoProcessor(
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${mercadopago.sandbox:true}") boolean sandbox,
            @Value("${mercadopago.back-url.success:http://localhost:5173/pagos/exitoso}") String backUrlSuccess,
            @Value("${mercadopago.back-url.failure:http://localhost:5173/pagos/fallido}") String backUrlFailure,
            @Value("${mercadopago.back-url.pending:http://localhost:5173/pagos/pendiente}") String backUrlPending,
            @Value("${mercadopago.notification-url:}") String notificationUrl
    ) {
        this(accessToken, sandbox, backUrlSuccess, backUrlFailure, backUrlPending, notificationUrl,
                new PreferenceClient(), new PaymentClient());
    }

    public MercadoPagoProcessor(
            String accessToken,
            boolean sandbox,
            String backUrlSuccess,
            String backUrlFailure,
            String backUrlPending,
            String notificationUrl,
            PreferenceClient preferenceClient,
            PaymentClient paymentClient
    ) {
        this.accessToken = accessToken != null ? accessToken.trim() : "";
        this.sandbox = sandbox;
        this.backUrlSuccess = backUrlSuccess;
        this.backUrlFailure = backUrlFailure;
        this.backUrlPending = backUrlPending;
        this.notificationUrl = notificationUrl != null ? notificationUrl.trim() : "";
        this.preferenceClient = preferenceClient;
        this.paymentClient = paymentClient;
    }

    @Override
    public ResultadoPago procesar(SolicitudPago solicitud) {
        if (accessToken.isBlank()) {
            String preferenceId = "MP-PREF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String simulatedUrl = "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preferenceId;
            log.info("Mercado Pago en modo simulación local (sin access-token configurado). Preferencia: {}", preferenceId);
            return new ResultadoPago(
                    EstadoPago.PENDIENTE,
                    preferenceId,
                    "Preferencia de MercadoPago generada (modo simulación local)",
                    simulatedUrl
            );
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            String titulo = (solicitud.descripcion() != null && !solicitud.descripcion().isBlank())
                    ? solicitud.descripcion()
                    : construirTituloPorDefecto(solicitud);

            String externalReference = solicitud.pagoId() != null
                    ? String.valueOf(solicitud.pagoId())
                    : UUID.randomUUID().toString();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(externalReference)
                    .title(titulo)
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(solicitud.monto())
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(backUrlSuccess)
                    .failure(backUrlFailure)
                    .pending(backUrlPending)
                    .build();

            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(externalReference);

            if (!notificationUrl.isBlank()) {
                requestBuilder.notificationUrl(notificationUrl);
            }

            Preference preference = preferenceClient.create(requestBuilder.build());

            String checkoutUrl = (sandbox && preference.getSandboxInitPoint() != null && !preference.getSandboxInitPoint().isBlank())
                    ? preference.getSandboxInitPoint()
                    : preference.getInitPoint();

            log.info("Preferencia real de Mercado Pago creada exitosamente: id={}, externalReference={}",
                    preference.getId(), externalReference);

            return new ResultadoPago(
                    EstadoPago.PENDIENTE,
                    preference.getId(),
                    "Preferencia de MercadoPago generada exitosamente",
                    checkoutUrl
            );
        } catch (MPApiException apiEx) {
            String detalle = apiEx.getApiResponse() != null ? apiEx.getApiResponse().getContent() : apiEx.getMessage();
            log.error("Error de API de Mercado Pago al crear preferencia (status {}): {}", apiEx.getStatusCode(), detalle);
            throw new IllegalStateException("No se pudo generar la preferencia de pago en Mercado Pago: " + detalle, apiEx);
        } catch (MPException ex) {
            log.error("Error de conexión con el SDK de Mercado Pago: {}", ex.getMessage());
            throw new IllegalStateException("Error de comunicación con Mercado Pago: " + ex.getMessage(), ex);
        }
    }

    public DatosPagoExternoMP consultarPagoExterno(Long mpPaymentId) {
        if (accessToken.isBlank()) {
            throw new IllegalStateException("No se puede consultar un pago real de Mercado Pago sin configurar mercadopago.access-token");
        }
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            Payment payment = paymentClient.get(mpPaymentId);
            String extRef = payment.getExternalReference();
            Long pagoLocalId = (extRef != null && !extRef.isBlank()) ? Long.valueOf(extRef.trim()) : null;
            return new DatosPagoExternoMP(pagoLocalId, payment.getStatus());
        } catch (MPApiException apiEx) {
            String detalle = apiEx.getApiResponse() != null ? apiEx.getApiResponse().getContent() : apiEx.getMessage();
            log.error("Error de API de Mercado Pago al consultar pago {} (status {}): {}", mpPaymentId, apiEx.getStatusCode(), detalle);
            throw new IllegalStateException("No se pudo consultar el pago en Mercado Pago: " + detalle, apiEx);
        } catch (MPException ex) {
            log.error("Error del SDK de Mercado Pago al consultar pago {}: {}", mpPaymentId, ex.getMessage());
            throw new IllegalStateException("Error al consultar el pago en Mercado Pago: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MetodoPago metodoSoportado() {
        return MetodoPago.MERCADOPAGO;
    }

    private String construirTituloPorDefecto(SolicitudPago solicitud) {
        if (solicitud.suscripcionId() != null) {
            return "Meru App - Suscripción #" + solicitud.suscripcionId();
        }
        if (solicitud.ordenId() != null) {
            return "Meru App - Orden Shop #" + solicitud.ordenId();
        }
        return "Meru App - Pago";
    }
}
