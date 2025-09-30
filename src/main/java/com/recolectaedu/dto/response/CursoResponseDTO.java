package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class CursoResponseDTO {
    private Integer id_curso;
    private String universidad;
    private String nombre;
    private String carrera;
}