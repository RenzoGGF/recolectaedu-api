package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ResenaRequestUpdateDTO(
        @NotNull(message = "El usuario tiene que estar registrado")
        Integer id_usuario,
        @NotNull(message = "El contenido no puede estar vacío.")
        String contenido,
        @NotNull(message = "El voto no puede estar vacío")
        Boolean es_positivo
) {}
