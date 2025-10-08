package com.recolectaedu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserRequestDTO {
    @NotBlank @Email @Size(max=255)
    private String email;

    @NotBlank @Size(min=8,max=255)
    private String password;

    private String rol; // opcional; si no, usa FREE por defecto

    private PerfilRequestDTO perfil; // puede ser null
}