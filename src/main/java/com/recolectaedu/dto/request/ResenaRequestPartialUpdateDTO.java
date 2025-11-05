package com.recolectaedu.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record ResenaRequestPartialUpdateDTO(
//        @Nullable
//        Integer id_usuario,
        @Nullable
        @Size(min = 5, max = 255, message = "El contenido debe tener entre 5 y 255 caracteres")
        String contenido,
        @Nullable
        Boolean es_positivo
) {
}
