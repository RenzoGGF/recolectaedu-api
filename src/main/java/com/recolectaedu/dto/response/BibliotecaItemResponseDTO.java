package com.recolectaedu.dto.response;

import lombok.Builder;

@Builder
public record BibliotecaItemResponseDTO(
        Integer id_biblioteca_recurso,
        Integer id_recurso,
        String titulo_recurso,
        String agregado_el
) {}
