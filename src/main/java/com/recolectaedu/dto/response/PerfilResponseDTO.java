package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class PerfilResponseDTO {
    private String nombre;
    private String apellidos;
    private Short ciclo;
    private String carrera;
    private String universidad;
}