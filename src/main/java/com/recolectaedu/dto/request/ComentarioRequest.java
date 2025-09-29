package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class ComentarioRequest {
    private String contenido;
    private Integer id_usuario;
    private Integer id_foro;
    private Integer id_comentario_padre; // Opcional
}