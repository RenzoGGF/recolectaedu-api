package com.recolectaedu.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForoResponseDTO {
    private Integer id_foro;
    private String titulo;
    private LocalDateTime creado_el;
    private Integer id_usuario;
}