package com.recolectaedu.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComentarioResponseDTO {
    private Integer id_comentario;
    private String contenido;
    private LocalDateTime creado_el;
    private Integer id_usuario;
}