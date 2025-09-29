package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class CursoResponse {
    private Integer id_curso;
    private String universidad;
    private String nombre;
    private String carrera;
}