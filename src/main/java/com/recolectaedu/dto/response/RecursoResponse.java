package com.recolectaedu.dto.response;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecursoResponse {
    private Integer id_recurso;
    private String titulo;
    private String descripcion;
    private String contenido;
    private FormatoRecurso formato;
    private Tipo_recurso tipo;
    private LocalDateTime creado_el;
    private Integer id_usuario;
    private Integer id_curso;
}