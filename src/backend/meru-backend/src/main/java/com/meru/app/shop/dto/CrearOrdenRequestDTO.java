package com.meru.app.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearOrdenRequestDTO(
        @NotNull(message = "El ID del alumno es obligatorio")
        Long alumnoId,

        @NotEmpty(message = "La orden debe contener al menos un producto")
        @Valid
        List<ItemOrdenRequestDTO> items
) {}
