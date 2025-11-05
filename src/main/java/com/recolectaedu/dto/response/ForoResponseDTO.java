package com.recolectaedu.dto.response;

import java.time.LocalDateTime;

public record ForoResponseDTO(
        Integer id_foro,
        String titulo,
        LocalDateTime creado_el,
        Integer id_usuario
) {
}