package com.recolectaedu.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForoRequestDTO {

    @NotNull(message = "El título no puede ser nulo")
    @Size(min = 10, message = "El título debe tener al menos 10 caracteres")
    String titulo;

    @NotNull(message = "El contenido no puede ser nulo")
    @Size(min = 20, message = "El contenido debe tener al menos 20 caracteres")
    private String contenido;

    @NotNull(message = "El usuario tiene que estar registrado")
    private Integer id_usuario;
}