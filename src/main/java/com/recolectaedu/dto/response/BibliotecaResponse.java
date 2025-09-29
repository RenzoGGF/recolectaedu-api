package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class BibliotecaResponse {
    private Integer id_biblioteca;
    private String nombre;
    private Integer id_usuario;
}