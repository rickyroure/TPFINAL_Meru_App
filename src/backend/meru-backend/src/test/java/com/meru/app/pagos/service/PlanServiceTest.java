package com.meru.app.pagos.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.pagos.domain.Plan;
import com.meru.app.pagos.dto.CrearPlanRequestDTO;
import com.meru.app.pagos.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    void crearPlan_debeGuardarYRetornarPlan() {
        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre", new BigDecimal("25000.00"), 30);

        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> {
            Plan p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Plan result = planService.crearPlan(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pase Libre", result.getNombre());
        assertEquals(new BigDecimal("25000.00"), result.getPrecio());
        assertEquals(30, result.getDuracionDias());
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    void listarPlanes_debeRetornarListaDePlanes() {
        Plan plan = new Plan();
        plan.setId(1L);
        plan.setNombre("Pase Libre");

        when(planRepository.findAll()).thenReturn(List.of(plan));

        List<Plan> result = planService.listarPlanes();

        assertEquals(1, result.size());
        assertEquals("Pase Libre", result.getFirst().getNombre());
        verify(planRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_cuandoExiste_debeRetornarPlan() {
        Plan plan = new Plan();
        plan.setId(1L);
        plan.setNombre("Pase Libre");

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        Plan result = planService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(planRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> planService.obtenerPorId(99L));
        verify(planRepository, times(1)).findById(99L);
    }

    @Test
    void actualizarPlan_cuandoExiste_debeModificarTarifaYRetornarPlan() {
        Plan existente = new Plan();
        existente.setId(1L);
        existente.setNombre("Pase Libre");
        existente.setPrecio(new BigDecimal("25000.00"));
        existente.setDuracionDias(30);

        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre Full", new BigDecimal("32000.00"), 30);

        when(planRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));

        Plan actualizado = planService.actualizarPlan(1L, request);

        assertEquals(1L, actualizado.getId());
        assertEquals("Pase Libre Full", actualizado.getNombre());
        assertEquals(new BigDecimal("32000.00"), actualizado.getPrecio());
        assertEquals(30, actualizado.getDuracionDias());
        verify(planRepository).save(existente);
    }

    @Test
    void actualizarPlan_cuandoNoExiste_debeLanzarExcepcion() {
        CrearPlanRequestDTO request = new CrearPlanRequestDTO("Pase Libre", new BigDecimal("30000.00"), 30);
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> planService.actualizarPlan(99L, request));
        verify(planRepository, never()).save(any());
    }
}

