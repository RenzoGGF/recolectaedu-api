package com.recolectaedu.dto.request;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RecursoPartialUpdateRequestDTO(
        @Nullable
        Integer id_curso,
        @Nullable
        @Size(max = 255)
        String titulo,
        @Nullable
        String descripcion,
        @Nullable
        String contenido,
        @Nullable
        FormatoRecurso formato,
        @Nullable
        Tipo_recurso tipo,
        @Nullable
        Integer ano,
        @Nullable
        Integer periodo
) {
}
