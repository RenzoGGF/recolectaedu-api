package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id_usuario;
    private String email;
    private String rol;
    private PerfilResponseDTO perfil;
}