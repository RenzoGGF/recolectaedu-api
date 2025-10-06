package com.recolectaedu.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record UsuarioStatsResponseDTO(
        long totalRecursosPublicados,
        long totalComentariosRealizados,
        long totalResenasRecibidas,
        Long totalResenasPositivas,
        Long totalResenasNegativas,
        long totalItemsBiblioteca
) {}