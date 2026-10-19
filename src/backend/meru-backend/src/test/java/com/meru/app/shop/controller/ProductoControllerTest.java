package com.meru.app.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meru.app.common.exception.GlobalExceptionHandler;
import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.security.SecurityConfig;
import com.meru.app.shop.domain.Producto;
import com.meru.app.shop.dto.ProductoRequestDTO;
import com.meru.app.shop.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void crearProducto_conRolAdmin_debeRetornar201Created() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Proteína Whey 1kg",
                new BigDecimal("35000.00"),
                20
        );

        Producto creado = new Producto();
        creado.setId(1L);
        creado.setNombre("Proteína Whey 1kg");
        creado.setPrecio(new BigDecimal("35000.00"));
        creado.setStock(20);

        when(productoService.crearProducto(any(ProductoRequestDTO.class))).thenReturn(creado);

        mockMvc.perform(post("/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Proteína Whey 1kg"))
                .andExpect(jsonPath("$.precio").value(35000.00))
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void crearProducto_conRolAlumno_debeRetornar403Forbidden() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Proteína Whey 1kg",
                new BigDecimal("35000.00"),
                20
        );

        mockMvc.perform(post("/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(productoService, never()).crearProducto(any());
    }

    @Test
    void crearProducto_conPrecioNegativo_debeRetornar400BadRequest() throws Exception {
        ProductoRequestDTO requestInvalido = new ProductoRequestDTO(
                "Producto Inválido",
                new BigDecimal("-100.00"),
                5
        );

        mockMvc.perform(post("/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).crearProducto(any());
    }

    @Test
    void actualizarProducto_conRolAdmin_debeRetornar200Ok() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Creatina Micronizada",
                new BigDecimal("28000.00"),
                12
        );

        Producto actualizado = new Producto();
        actualizado.setId(5L);
        actualizado.setNombre("Creatina Micronizada");
        actualizado.setPrecio(new BigDecimal("28000.00"));
        actualizado.setStock(12);

        when(productoService.actualizarProducto(eq(5L), any(ProductoRequestDTO.class))).thenReturn(actualizado);

        mockMvc.perform(put("/productos/5")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Creatina Micronizada"))
                .andExpect(jsonPath("$.stock").value(12));
    }

    @Test
    void actualizarProducto_cuandoNoExiste_debeRetornar404ProblemDetail() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO(
                "Creatina",
                new BigDecimal("28000.00"),
                10
        );

        when(productoService.actualizarProducto(eq(99L), any(ProductoRequestDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con ID: 99"));

        mockMvc.perform(put("/productos/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Producto no encontrado con ID: 99"));
    }

    @Test
    void listarProductos_conRolAlumno_debeRetornar200Ok() throws Exception {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Agua Mineral");
        p.setPrecio(new BigDecimal("1500.00"));
        p.setStock(30);

        when(productoService.listarProductos()).thenReturn(List.of(p));

        mockMvc.perform(get("/productos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_alumno"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Agua Mineral"));
    }
}
