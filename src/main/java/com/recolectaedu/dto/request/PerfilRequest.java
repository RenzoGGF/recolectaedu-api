package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class PerfilRequest {
    private String nombre;
    private String apellidos;
    private Short ciclo;
    private String carrera;
    private String universidad;
}