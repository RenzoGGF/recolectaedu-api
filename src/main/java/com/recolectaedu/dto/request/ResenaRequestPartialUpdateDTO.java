package com.recolectaedu.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ResenaRequestPartialUpdateDTO(
//        @Nullable
//        Integer id_usuario,
        @Nullable
        @Min(value = 5, message = "El contenido debe tener al menos 5 caracteres")
        @Max(value = 255, message = "El contenido debe tener como máximo 255 caracteres")
        String contenido,
        @Nullable
        Boolean es_positivo
) {
}
