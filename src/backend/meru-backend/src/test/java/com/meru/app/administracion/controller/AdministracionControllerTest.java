package com.meru.app.administracion.controller;

import com.meru.app.administracion.dto.ActividadComposicionDTO;
import com.meru.app.administracion.dto.AfluenciaDiaSemanaDTO;
import com.meru.app.administracion.dto.AfluenciaRangoHorarioDTO;
import com.meru.app.administracion.dto.IngresoMensualDTO;
import com.meru.app.administracion.dto.ReporteAfluenciaResponseDTO;
import com.meru.app.administracion.dto.ReporteIngresosResponseDTO;
import com.meru.app.administracion.service.ReporteActividadService;
import com.meru.app.administracion.service.ReporteAfluenciaService;
import com.meru.app.administracion.service.ReporteIngresosService;
import com.meru.app.common.exception.GlobalExceptionHandler;
import com.meru.app.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdministracionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdministracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteActividadService reporteActividadService;

    @MockitoBean
    private ReporteIngresosService reporteIngresosService;

    @MockitoBean
    private ReporteAfluenciaService reporteAfluenciaService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void obtenerComposicionPorActividad_conRolAdmin_debeRetornar200() throws Exception {
        List<ActividadComposicionDTO> datos = List.of(
                new ActividadComposicionDTO("CrossFit", 15L, 42L),
                new ActividadComposicionDTO("Musculación", 10L, 28L)
        );

        when(reporteActividadService.obtenerComposicionPorActividad()).thenReturn(datos);

        mockMvc.perform(get("/administracion/dashboard/actividades")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipoActividad").value("CrossFit"))
                .andExpect(jsonPath("$[0].cantidadAlumnos").value(15))
                .andExpect(jsonPath("$[0].totalParticipaciones").value(42))
                .andExpect(jsonPath("$[1].tipoActividad").value("Musculación"));
    }

    @Test
    void obtenerComposicionPorActividad_conRolAlumno_debeRetornar403() throws Exception {
        mockMvc.perform(get("/administracion/dashboard/actividades")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerReporteIngresos_conRolAdmin_debeRetornar200() throws Exception {
        ReporteIngresosResponseDTO reporte = new ReporteIngresosResponseDTO(
                2026,
                null,
                new BigDecimal("67500.50"),
                new BigDecimal("55000.00"),
                new BigDecimal("12500.50"),
                3L,
                List.of(new IngresoMensualDTO(
                        2026,
                        3,
                        new BigDecimal("37500.50"),
                        new BigDecimal("25000.00"),
                        new BigDecimal("12500.50"),
                        2L
                ))
        );

        when(reporteIngresosService.obtenerReporteIngresos(2026, null)).thenReturn(reporte);

        mockMvc.perform(get("/administracion/reportes/ingresos")
                        .param("anio", "2026")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(2026))
                .andExpect(jsonPath("$.totalIngresos").value(67500.50))
                .andExpect(jsonPath("$.ingresosSuscripciones").value(55000.00))
                .andExpect(jsonPath("$.ingresosShop").value(12500.50))
                .andExpect(jsonPath("$.desgloseMensual.length()").value(1))
                .andExpect(jsonPath("$.desgloseMensual[0].mes").value(3));
    }

    @Test
    void obtenerReporteIngresos_conRolProfesor_debeRetornar403() throws Exception {
        mockMvc.perform(get("/administracion/reportes/ingresos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_profesor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerReporteAfluencia_conRolAdmin_debeRetornar200() throws Exception {
        ReporteAfluenciaResponseDTO reporte = new ReporteAfluenciaResponseDTO(
                5L,
                List.of(new AfluenciaDiaSemanaDTO("LUNES", 1, 3L)),
                List.of(new AfluenciaRangoHorarioDTO("18:00 - 19:00", 18, 4L))
        );

        when(reporteAfluenciaService.obtenerReporteAfluencia()).thenReturn(reporte);

        mockMvc.perform(get("/administracion/reportes/afluencia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConcurrencias").value(5))
                .andExpect(jsonPath("$.porDiaSemana[0].diaSemana").value("LUNES"))
                .andExpect(jsonPath("$.porDiaSemana[0].cantidadConcurrencias").value(3))
                .andExpect(jsonPath("$.porRangoHorario[0].rangoHorario").value("18:00 - 19:00"))
                .andExpect(jsonPath("$.porRangoHorario[0].cantidadConcurrencias").value(4));
    }

    @Test
    void obtenerReporteAfluencia_conRolAlumno_debeRetornar403() throws Exception {
        mockMvc.perform(get("/administracion/reportes/afluencia")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno"))))
                .andExpect(status().isForbidden());
    }
}
