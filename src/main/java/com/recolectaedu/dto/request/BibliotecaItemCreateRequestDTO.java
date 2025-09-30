package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BibliotecaItemCreateRequestDTO(
    @NotNull(message = "El Id del recurso no puede estar vacío.")
    Integer id_recurso
) {}
