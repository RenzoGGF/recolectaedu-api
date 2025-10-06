package com.recolectaedu.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecursoValoradoResponseDTO(
        Integer id_recurso,
        String titulo,
        String descripcion,
        Integer ano,
        Integer periodo,
        Integer votos_utiles,
        Integer votos_no_utiles,
        Integer votos_netos,
        LocalDateTime actualizado_el
) {
}
