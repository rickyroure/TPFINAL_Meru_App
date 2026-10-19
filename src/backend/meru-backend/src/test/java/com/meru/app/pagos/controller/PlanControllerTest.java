package com.meru.app.pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meru.app.common.exception.GlobalExceptionHandler;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.dto.CrearPlanRequestDTO;
import com.meru.app.pagos.service.PlanService;
import com.meru.app.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PlanService planService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void actualizarPlan_conRolAdmin_debeRetornar200() throws Exception {
        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre Full", new BigDecimal("32000.00"), 30);

        Plan actualizado = new Plan();
        actualizado.setId(1L);
        actualizado.setNombre("Pase Libre Full");
        actualizado.setPrecio(new BigDecimal("32000.00"));
        actualizado.setDuracionDias(30);

        when(planService.actualizarPlan(eq(1L), any(CrearPlanRequestDTO.class))).thenReturn(actualizado);

        mockMvc.perform(put("/planes/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pase Libre Full"))
                .andExpect(jsonPath("$.precio").value(32000.00));
    }

    @Test
    void actualizarPlan_conRolAlumno_debeRetornar403() throws Exception {
        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre", new BigDecimal("100.00"), 30);

        mockMvc.perform(put("/planes/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarPlan_cuandoNoExiste_debeRetornar404ProblemDetail() throws Exception {
        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre", new BigDecimal("32000.00"), 30);

        when(planService.actualizarPlan(eq(99L), any(CrearPlanRequestDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("Plan no encontrado con ID: 99"));

        mockMvc.perform(put("/planes/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Plan no encontrado con ID: 99"));
    }
}
