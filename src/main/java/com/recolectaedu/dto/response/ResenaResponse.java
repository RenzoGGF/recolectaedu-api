package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class ResenaResponse {
    private Integer id_reseña;
    private String titulo;
    private Boolean es_positivo;
    private Integer id_usuario;
    private Integer id_recurso;
}