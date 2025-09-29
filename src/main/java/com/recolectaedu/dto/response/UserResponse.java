package com.recolectaedu.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Integer id_usuario;
    private String email;
    private String rol;
    private PerfilResponse perfil;
}