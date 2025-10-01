package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String password;
    private PerfilRequestDTO perfil;
}

