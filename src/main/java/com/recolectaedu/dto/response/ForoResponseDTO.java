package com.recolectaedu.dto.response;

import java.time.LocalDateTime;

public record ForoResponseDTO(
        Integer id_foro,
        String titulo,
        String contenido,
        LocalDateTime creado_el,
        Integer id_usuario,
        String nombre,
        String apellido
) {
}