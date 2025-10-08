package com.recolectaedu.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class PerfilResponseDTO {
    @NotBlank @Size(max=255)
    private Integer id_usuario;

    @NotBlank @Size(max=255)
    private String nombre;

    @NotBlank
    @Size(max=255)
    private String apellidos;

    @NotBlank @Size(max=255)
    private String universidad;

    @NotBlank @Size(max=255)
    private String carrera;

    @NotBlank @Size(max=10)
    private Short ciclo;
}