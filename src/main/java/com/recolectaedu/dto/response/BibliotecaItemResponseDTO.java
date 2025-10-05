package com.recolectaedu.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BibliotecaItemResponseDTO(
        Integer id_biblioteca_recurso,
        String titulo_recurso,
        LocalDateTime agregado_el
) {}
