package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class ForoRequestDTO {
    private String titulo;
    private String contenido;
    private Integer id_usuario;
}