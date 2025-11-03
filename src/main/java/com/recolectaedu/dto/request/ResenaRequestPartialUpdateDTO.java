package com.recolectaedu.dto.request;

import jakarta.annotation.Nullable;

public record ResenaRequestPartialUpdateDTO(
//        @Nullable
//        Integer id_usuario,
        @Nullable
        String contenido,
        @Nullable
        Boolean es_positivo
) {
}
