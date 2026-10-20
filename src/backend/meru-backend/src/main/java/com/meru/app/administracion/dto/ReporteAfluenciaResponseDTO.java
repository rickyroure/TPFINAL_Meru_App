package com.meru.app.administracion.dto;

import java.util.List;

public record ReporteAfluenciaResponseDTO(
        long totalConcurrencias,
        List<AfluenciaDiaSemanaDTO> porDiaSemana,
        List<AfluenciaRangoHorarioDTO> porRangoHorario
) {}
