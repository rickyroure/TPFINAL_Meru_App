package com.meru.app.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meru.app.alumnos.domain.Alumno;
import com.meru.app.common.exception.GlobalExceptionHandler;
import com.meru.app.common.exception.StockInsuficienteException;
import com.meru.app.security.SecurityConfig;
import com.meru.app.shop.domain.EstadoOrden;
import com.meru.app.shop.domain.ItemOrden;
import com.meru.app.shop.domain.Orden;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.CrearOrdenRequestDTO;
import com.meru.app.shop.dto.ItemOrdenRequestDTO;
import com.meru.app.shop.service.OrdenService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OrdenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrdenService ordenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void crearOrden_conDatosValidos_debeRetornar201Created() throws Exception {
        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO(
                10L,
                List.of(new ItemOrdenRequestDTO(1L, 2))
        );

        Alumno alumno = new Alumno();
        alumno.setId(10L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Proteína Whey");
        producto.setPrecio(new BigDecimal("30000.00"));

        Orden orden = new Orden();
        orden.setId(100L);
        orden.setAlumno(alumno);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setTotal(new BigDecimal("60000.00"));
        orden.setFecha(LocalDateTime.now());

        ItemOrden item = new ItemOrden();
        item.setId(1L);
        item.setOrden(orden);
        item.setProducto(producto);
        item.setCantidad(2);
        item.setPrecioUnitario(new BigDecimal("30000.00"));
        orden.setItems(List.of(item));

        when(ordenService.crearOrden(any(CrearOrdenRequestDTO.class))).thenReturn(orden);

        mockMvc.perform(post("/ordenes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.alumnoId").value(10))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(60000.00))
                .andExpect(jsonPath("$.items[0].productoNombre").value("Proteína Whey"))
                .andExpect(jsonPath("$.items[0].subtotal").value(60000.00));
    }

    @Test
    void crearOrden_cuandoStockEsInsuficiente_debeRetornar409ConflictProblemDetail() throws Exception {
        CrearOrdenRequestDTO request = new CrearOrdenRequestDTO(
                10L,
                List.of(new ItemOrdenRequestDTO(1L, 5))
        );

        when(ordenService.crearOrden(any(CrearOrdenRequestDTO.class)))
                .thenThrow(new StockInsuficienteException("Stock insuficiente para el producto 'Proteína Whey'"));

        mockMvc.perform(post("/ordenes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Stock insuficiente para el producto 'Proteína Whey'"));
    }

    @Test
    void crearOrden_conListaItemsVacia_debeRetornar400BadRequest() throws Exception {
        CrearOrdenRequestDTO requestInvalido = new CrearOrdenRequestDTO(10L, List.of());

        mockMvc.perform(post("/ordenes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(ordenService, never()).crearOrden(any());
    }

    @Test
    void listarPorAlumno_debeRetornar200Ok() throws Exception {
        Alumno alumno = new Alumno();
        alumno.setId(10L);

        Orden orden = new Orden();
        orden.setId(100L);
        orden.setAlumno(alumno);
        orden.setEstado(EstadoOrden.PAGADA);
        orden.setTotal(new BigDecimal("2500.00"));
        orden.setFecha(LocalDateTime.now());
        orden.setItems(List.of());

        when(ordenService.listarPorAlumno(10L)).thenReturn(List.of(orden));

        mockMvc.perform(get("/ordenes/alumno/10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].estado").value("PAGADA"));
    }
}
