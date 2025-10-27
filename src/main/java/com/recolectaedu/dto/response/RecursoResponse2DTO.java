package com.recolectaedu.dto.response;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecursoResponse2DTO(
        Integer id_recurso,
        String titulo,
        String descripcion,
        String contenido,
        FormatoRecurso formato,
        Tipo_recurso tipo,
        LocalDateTime creado_el,
        Integer id_usuario,
        Integer id_curso,
        String autorNombre
) {}
