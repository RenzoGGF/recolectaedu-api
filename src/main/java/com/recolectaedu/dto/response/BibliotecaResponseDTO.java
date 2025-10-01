package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class BibliotecaResponseDTO {
    private Integer id_biblioteca;
    private String nombre;
    private Integer id_usuario;
}