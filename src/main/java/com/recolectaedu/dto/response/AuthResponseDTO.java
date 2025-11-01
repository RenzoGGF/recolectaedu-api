package com.recolectaedu.dto.response;

public record AuthResponseDTO(
        String token,
        String type,
        String email,
        String name
) {
    public AuthResponseDTO(String token, String email, String name) {
        this(token, "Bearer", email, name);
    }
}
