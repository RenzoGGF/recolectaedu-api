package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class ResenaRequestDTO {
    private String titulo;
    private String contenido;
    private Boolean es_positivo;
    private Integer id_usuario;
    private Integer id_recurso;
}