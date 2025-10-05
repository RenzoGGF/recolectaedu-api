package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BibliotecaItemCreateRequestDTO(
        @NotNull(message = "El id de la biblioteca no puede estar vacío.")
        Integer id_biblioteca,
        @NotNull(message = "El Id del recurso no puede estar vacío.")
        Integer id_recurso
) {}
