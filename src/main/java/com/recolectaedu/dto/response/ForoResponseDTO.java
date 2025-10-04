package com.recolectaedu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForoResponseDTO {
    private Integer id_foro;
    private String titulo;
    private LocalDateTime creado_el;
    private Integer id_usuario;
}