package com.meru.app.administracion.controller;

import com.meru.app.administracion.dto.ActividadComposicionDTO;
import com.meru.app.administracion.dto.ReporteIngresosResponseDTO;
import com.meru.app.administracion.service.ReporteActividadService;
import com.meru.app.administracion.service.ReporteIngresosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/administracion")
@RequiredArgsConstructor
@Tag(name = "Administración y Reportes", description = "Dashboard gerencial y reportes estadísticos para el administrador")
public class AdministracionController {

    private final ReporteActividadService reporteActividadService;
    private final ReporteIngresosService reporteIngresosService;

    @GetMapping("/dashboard/actividades")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Composición de alumnos por actividad (AD1) agregando reservas a clases y check-ins libres")
    public ResponseEntity<List<ActividadComposicionDTO>> obtenerComposicionPorActividad() {
        return ResponseEntity.ok(reporteActividadService.obtenerComposicionPorActividad());
    }

    @GetMapping("/reportes/ingresos")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Reporte de ingresos mensuales y anuales (AD2) agregando pagos aprobados por período")
    public ResponseEntity<ReporteIngresosResponseDTO> obtenerReporteIngresos(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes
    ) {
        return ResponseEntity.ok(reporteIngresosService.obtenerReporteIngresos(anio, mes));
    }
}

