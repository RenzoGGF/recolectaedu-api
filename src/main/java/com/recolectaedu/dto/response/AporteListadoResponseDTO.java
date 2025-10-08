package com.recolectaedu.dto.response;

import com.recolectaedu.model.enums.Tipo_recurso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AporteListadoResponseDTO {
    private Integer id;
    private String titulo;
    private Tipo_recurso tipo;
    private Integer cursoId;
    private String cursoNombre;
    private String universidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
