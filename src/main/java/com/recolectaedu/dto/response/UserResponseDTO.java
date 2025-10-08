package com.recolectaedu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class UserResponseDTO {
    private Integer id_usuario;
    private String email;
    private String role;
    private PerfilResponseDTO profile; // puede ser null
}
