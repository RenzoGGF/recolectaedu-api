package com.recolectaedu.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
    private PerfilRequest perfil;
}

