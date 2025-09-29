package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class ForoRequest {
    private String titulo;
    private String contenido;
    private Integer id_usuario;
}