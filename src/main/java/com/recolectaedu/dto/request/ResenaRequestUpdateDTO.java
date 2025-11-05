package com.recolectaedu.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record ResenaRequestUpdateDTO(
//        @NotNull(message = "El usuario tiene que estar registrado")
//        Integer id_usuario,
        @NotBlank(message = "El contenido no puede estar vacío.")
        @Size(min = 5, max = 255, message = "El contenido debe tener entre 5 y 255 caracteres")
        String contenido,
        @NotNull(message = "El voto no puede estar vacío")
        Boolean es_positivo
) {}
