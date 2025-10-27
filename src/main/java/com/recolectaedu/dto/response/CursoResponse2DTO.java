package com.recolectaedu.dto.response;


import lombok.Builder;

@Builder
public record CursoResponse2DTO(
        Integer id_curso,
        String universidad,
        String nombre,
        String carrera,
        Long totalRecursos
) { }
