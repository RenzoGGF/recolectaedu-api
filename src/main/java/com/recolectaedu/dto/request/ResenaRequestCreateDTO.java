package com.recolectaedu.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ResenaRequestCreateDTO(
//        @NotNull(message = "El usuario tiene que estar registrado")
//        Integer id_usuario,
        @NotNull(message = "El recurso tiene que estar registrado")
        Integer id_recurso,
        @NotNull(message = "El contenido no puede estar vacío.")
        @Min(value = 5, message = "El contenido debe tener al menos 5 caracteres")
        @Max(value = 255, message = "El contenido debe tener como máximo 255 caracteres")
        String contenido,
        @NotNull(message = "El voto no puede estar vacío")
        Boolean es_positivo
) {}