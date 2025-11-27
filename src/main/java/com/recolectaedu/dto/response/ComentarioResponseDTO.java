package com.recolectaedu.dto.response;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Integer id_comentario,
        String contenido,
        LocalDateTime creado_el,
        String nombreAutor,
        String apellidoAutor,
        Integer id_usuario,
        Integer id_comentario_padre
) {}