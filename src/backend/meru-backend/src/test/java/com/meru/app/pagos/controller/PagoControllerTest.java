package com.meru.app.pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meru.app.common.exception.GlobalExceptionHandler;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.EstadoPago;
import com.meru.app.pagos.domain.MetodoPago;
import com.meru.app.pagos.domain.Pago;
import com.meru.app.pagos.dto.PagoResponseDTO;
import com.meru.app.pagos.dto.ProcesarPagoRequestDTO;
import com.meru.app.pagos.service.PagoService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void procesarPago_deOrdenDelShop_debeRetornar201Created() throws Exception {
        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("35000.00"),
                MetodoPago.EFECTIVO,
                null,
                45L
        );

        PagoResponseDTO responseDTO = new PagoResponseDTO(
                700L,
                null,
                45L,
                new BigDecimal("35000.00"),
                MetodoPago.EFECTIVO,
                EstadoPago.APROBADO,
                LocalDateTime.now(),
                "REC-EFECTIVO-123",
                "Pago en efectivo aprobado"
        );

        when(pagoService.procesarPago(any(ProcesarPagoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/pagos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(700))
                .andExpect(jsonPath("$.ordenId").value(45))
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.metodo").value("EFECTIVO"));
    }

    @Test
    void procesarPago_cuandoOrdenNoExiste_debeRetornar404ProblemDetail() throws Exception {
        ProcesarPagoRequestDTO request = new ProcesarPagoRequestDTO(
                new BigDecimal("35000.00"),
                MetodoPago.MERCADOPAGO,
                null,
                999L
        );

        when(pagoService.procesarPago(any(ProcesarPagoRequestDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("Orden no encontrada con ID: 999"));

        mockMvc.perform(post("/pagos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Orden no encontrada con ID: 999"));
    }

    @Test
    void listarPorOrden_debeRetornar200Ok() throws Exception {
        Pago pago = new Pago();
        pago.setId(700L);
        pago.setOrdenId(45L);
        pago.setMonto(new BigDecimal("35000.00"));
        pago.setMetodo(MetodoPago.EFECTIVO);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFecha(LocalDateTime.now());

        when(pagoService.listarPorOrden(45L)).thenReturn(List.of(pago));

        mockMvc.perform(get("/pagos/orden/45")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(700))
                .andExpect(jsonPath("$[0].ordenId").value(45));
    }
}
