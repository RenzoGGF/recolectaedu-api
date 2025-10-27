package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ForoRequestDTO(
        @NotNull(message = "El título no puede ser nulo")
        @Size(min = 10, message = "El título debe tener al menos 10 caracteres")
        String titulo,
        @NotNull(message = "El contenido no puede ser nulo")
        @Size(min = 20, message = "El contenido debe tener al menos 20 caracteres")
        String contenido,
        @NotNull(message = "El usuario tiene que estar registrado")
        Integer id_usuario
) {
}