package com.recolectaedu.dto.request;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RecursoUpdateRequestDTO(
        @NotNull
        Integer id_curso,
        @NotBlank
        @Size(max = 255)
        String titulo,
        @NotBlank
        String descripcion,
        @NotBlank
        String contenido,
        @NotNull
        FormatoRecurso formato,
        @NotNull
        Tipo_recurso tipo,
        @NotNull
        @Min(1900) @Max(2100)
        Integer ano,
        @NotNull
        Integer periodo
) {
}
