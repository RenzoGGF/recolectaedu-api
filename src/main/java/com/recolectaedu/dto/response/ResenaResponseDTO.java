package com.recolectaedu.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ResenaResponseDTO(
        Integer id_resena,
        String contenido,
        Boolean es_positivo,
        String nombre_autor,
        String titulo_recurso,
        LocalDateTime creado_el,
        LocalDateTime actualizado_el
) {}