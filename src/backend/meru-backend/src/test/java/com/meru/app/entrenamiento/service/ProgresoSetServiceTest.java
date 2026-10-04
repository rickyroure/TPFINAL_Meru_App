package com.meru.app.entrenamiento.service;

import com.meru.app.common.exception.RecursoNoEncontradoException;
import com.meru.app.entrenamiento.domain.PlanEjercicio;
import com.meru.app.entrenamiento.domain.ProgresoSet;
import com.meru.app.entrenamiento.dto.RegistrarProgresoSetRequestDTO;
import com.meru.app.entrenamiento.repository.PlanEjercicioRepository;
import com.meru.app.entrenamiento.repository.ProgresoSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgresoSetServiceTest {

    @Mock
    private ProgresoSetRepository progresoSetRepository;

    @Mock
    private PlanEjercicioRepository planEjercicioRepository;

    @InjectMocks
    private ProgresoSetService progresoSetService;

    @Test
    void registrarProgresoSet_debeGuardarYRetornar_cuandoPlanEjercicioExiste() {
        Long planId = 1L;
        PlanEjercicio plan = new PlanEjercicio();
        plan.setId(planId);

        RegistrarProgresoSetRequestDTO request = new RegistrarProgresoSetRequestDTO(
                planId, 1, new BigDecimal("80.00"), 10, LocalDate.of(2026, 6, 2)
        );

        when(planEjercicioRepository.findById(planId)).thenReturn(Optional.of(plan));

        ProgresoSet saved = new ProgresoSet();
        saved.setId(100L);
        saved.setPlanEjercicio(plan);
        saved.setNumeroSerie(1);
        saved.setPesoRealizado(new BigDecimal("80.00"));
        saved.setRepeticionesRealizadas(10);
        saved.setFecha(LocalDate.of(2026, 6, 2));

        when(progresoSetRepository.save(any(ProgresoSet.class))).thenReturn(saved);

        ProgresoSet result = progresoSetService.registrarProgresoSet(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(planId, result.getPlanEjercicio().getId());
        assertEquals(1, result.getNumeroSerie());
        assertEquals(new BigDecimal("80.00"), result.getPesoRealizado());
        verify(progresoSetRepository, times(1)).save(any(ProgresoSet.class));
    }

    @Test
    void registrarProgresoSet_debeLanzarExcepcion_cuandoPlanEjercicioNoExiste() {
        Long planId = 99L;
        RegistrarProgresoSetRequestDTO request = new RegistrarProgresoSetRequestDTO(
                planId, 1, new BigDecimal("80.00"), 10, null
        );

        when(planEjercicioRepository.findById(planId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> progresoSetService.registrarProgresoSet(request));
        verify(progresoSetRepository, never()).save(any());
    }

    @Test
    void listarPorPlanEjercicio_debeRetornarLista_cuandoPlanExiste() {
        Long planId = 1L;
        PlanEjercicio plan = new PlanEjercicio();
        plan.setId(planId);

        ProgresoSet p1 = new ProgresoSet();
        p1.setId(10L);
        p1.setPlanEjercicio(plan);

        when(planEjercicioRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(progresoSetRepository.findByPlanEjercicioIdOrderByFechaAscNumeroSerieAsc(planId))
                .thenReturn(List.of(p1));

        List<ProgresoSet> result = progresoSetService.listarPorPlanEjercicio(planId);

        assertEquals(1, result.size());
        verify(progresoSetRepository, times(1)).findByPlanEjercicioIdOrderByFechaAscNumeroSerieAsc(planId);
    }

    @Test
    void listarPorPlanEjercicio_debeLanzarExcepcion_cuandoPlanNoExiste() {
        Long planId = 99L;
        when(planEjercicioRepository.findById(planId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> progresoSetService.listarPorPlanEjercicio(planId));
        verify(progresoSetRepository, never()).findByPlanEjercicioIdOrderByFechaAscNumeroSerieAsc(any());
    }
}
