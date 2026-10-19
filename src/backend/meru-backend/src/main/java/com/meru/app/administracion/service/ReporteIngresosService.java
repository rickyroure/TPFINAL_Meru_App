package com.meru.app.administracion.service;

import com.meru.app.administracion.dto.IngresoMensualDTO;
import com.meru.app.administracion.dto.ReporteIngresosResponseDTO;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteIngresosService {

    private static final BigDecimal ZERO_MONETARY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public ReporteIngresosResponseDTO obtenerReporteIngresos(Integer anio, Integer mes) {
        int anioFiltro = (anio != null) ? anio : LocalDate.now().getYear();
        if (anioFiltro < 2000 || anioFiltro > 2100) {
            throw new IllegalArgumentException("El año indicado para el reporte es inválido");
        }
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new IllegalArgumentException("El mes debe estar comprendido entre 1 y 12");
        }

        LocalDateTime desde;
        LocalDateTime hasta;

        if (mes != null) {
            YearMonth yearMonth = YearMonth.of(anioFiltro, mes);
            desde = yearMonth.atDay(1).atStartOfDay();
            hasta = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        } else {
            desde = LocalDateTime.of(anioFiltro, 1, 1, 0, 0, 0);
            hasta = LocalDateTime.of(anioFiltro, 12, 31, 23, 59, 59);
        }

        List<Pago> pagosAprobados = pagoRepository.findByEstadoAndFechaBetweenOrderByFechaAsc(
                EstadoPago.APROBADO, desde, hasta
        );

        BigDecimal totalIngresos = ZERO_MONETARY;
        BigDecimal ingresosSuscripciones = ZERO_MONETARY;
        BigDecimal ingresosShop = ZERO_MONETARY;

        for (Pago pago : pagosAprobados) {
            BigDecimal monto = pago.getMonto() != null
                    ? pago.getMonto().setScale(2, RoundingMode.HALF_UP)
                    : ZERO_MONETARY;
            totalIngresos = totalIngresos.add(monto);
            if (pago.getOrdenId() != null) {
                ingresosShop = ingresosShop.add(monto);
            } else {
                ingresosSuscripciones = ingresosSuscripciones.add(monto);
            }
        }

        Map<Integer, List<Pago>> pagosPorMes = pagosAprobados.stream()
                .collect(Collectors.groupingBy(p -> p.getFecha().getMonthValue()));

        List<IngresoMensualDTO> desgloseMensual = pagosPorMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> {
                    int mesActual = entry.getKey();
                    List<Pago> pagosMes = entry.getValue();
                    BigDecimal totalMes = ZERO_MONETARY;
                    BigDecimal suscMes = ZERO_MONETARY;
                    BigDecimal shopMes = ZERO_MONETARY;

                    for (Pago p : pagosMes) {
                        BigDecimal monto = p.getMonto() != null
                                ? p.getMonto().setScale(2, RoundingMode.HALF_UP)
                                : ZERO_MONETARY;
                        totalMes = totalMes.add(monto);
                        if (p.getOrdenId() != null) {
                            shopMes = shopMes.add(monto);
                        } else {
                            suscMes = suscMes.add(monto);
                        }
                    }

                    return new IngresoMensualDTO(
                            anioFiltro,
                            mesActual,
                            totalMes,
                            suscMes,
                            shopMes,
                            pagosMes.size()
                    );
                })
                .toList();

        return new ReporteIngresosResponseDTO(
                anioFiltro,
                mes,
                totalIngresos,
                ingresosSuscripciones,
                ingresosShop,
                pagosAprobados.size(),
                desgloseMensual
        );
    }
}
