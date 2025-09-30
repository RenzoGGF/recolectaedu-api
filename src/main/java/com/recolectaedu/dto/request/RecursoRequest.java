package com.recolectaedu.dto.request;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import lombok.Data;

@Data
public class RecursoRequest {
    private String titulo;
    private String descripcion;
    private String contenido;
    private FormatoRecurso formato;
    private Tipo_recurso tipo;
    private Integer ano;
    private Integer periodo;
    private Integer id_usuario;
    private Integer id_curso;
}