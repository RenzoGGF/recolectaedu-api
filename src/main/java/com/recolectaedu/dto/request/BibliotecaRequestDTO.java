package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BibliotecaRequestDTO(
        @NotNull(message = "El id del recurso no puede estar vacío.")
        Integer id_usuario
) {}