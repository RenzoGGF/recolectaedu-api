package com.recolectaedu.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioRequestDTO(
        @NotBlank(message = "El contenido no puede estar vacío")
        String contenido,

        @NotNull(message = "El ID del foro es obligatorio")
        Integer id_foro,

        Integer id_comentario_padre
) {}