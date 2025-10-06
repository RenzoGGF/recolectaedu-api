package com.recolectaedu.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilRequestDTO {
    @NotBlank
    @Size(max=255)
    private String nombre;

    @NotBlank @Size(max=255)
    private String apellidos;

    @NotBlank @Size(max=255)
    private String universidad;

    @NotBlank @Size(max=255)
    private String carrera;

    @NotNull @Min(1) @Max(10)
    private Short ciclo;
}