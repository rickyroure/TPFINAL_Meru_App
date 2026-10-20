package com.meru.app.pagos.service;

import com.meru.app.pagos.domain.EstadoSuscripcion;
import com.meru.app.pagos.domain.Suscripcion;
import com.meru.app.pagos.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuscripcionVencimientoScheduler {

    private static final int DIAS_ANTICIPACION_ALERTA = 3;

    private final SuscripcionRepository suscripcionRepository;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public int notificarSuscripcionesProximasAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ANTICIPACION_ALERTA);

        List<Suscripcion> proximasAVencer = suscripcionRepository.findByEstadoAndFechaFinBetween(
                EstadoSuscripcion.ACTIVA, hoy, limite
        );

        for (Suscripcion suscripcion : proximasAVencer) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, suscripcion.getFechaFin());
            Long alumnoId = suscripcion.getAlumno() != null ? suscripcion.getAlumno().getId() : null;
            log.info("Notificación fire-and-forget (PG5): La suscripción ID {} del alumno ID {} vence en {} días ({})",
                    suscripcion.getId(), alumnoId, diasRestantes, suscripcion.getFechaFin());
        }

        return proximasAVencer.size();
    }
}
