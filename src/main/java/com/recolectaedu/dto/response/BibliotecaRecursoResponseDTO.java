package com.recolectaedu.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BibliotecaRecursoResponseDTO(
        Integer id_biblioteca_recurso,
        String titulo_recurso,
        Integer id_recurso,
        LocalDateTime agregado_el
) {}
