package com.meru.app.administracion.controller;

import com.meru.app.administracion.dto.ActividadComposicionDTO;
import com.meru.app.administracion.service.ReporteActividadService;
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
}
