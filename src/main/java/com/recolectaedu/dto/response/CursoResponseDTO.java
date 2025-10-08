package com.recolectaedu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponseDTO {
    private Integer id_curso;
    private String universidad;
    private String nombre;
    private String carrera;
    private Long totalRecursos;

}