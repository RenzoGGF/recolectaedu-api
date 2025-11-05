package com.recolectaedu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "El email no puede estar en blanco")
        @Email(message = "El email debe ser válido")
        String email,

        @NotBlank(message = "La contraseña no puede estar en blanco")
        String password
) {
}
