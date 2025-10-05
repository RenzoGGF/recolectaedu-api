package com.recolectaedu.dto.response;

import lombok.Builder;

@Builder
public record BibliotecaResponseDTO(
        Integer id_biblioteca,
        String nombre,
        Integer id_usuario
) {}