package com.recolectaedu.controller;

import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.UsuarioStatsResponseDTO;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Por hacer: devolver un ResponseDTO sin password_hash
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO req) {
        var resp = usuarioService.registrarUsuario(req); // que devuelva UserResponseDTO
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/{id_usuario}/estadisticas")
    public ResponseEntity<UsuarioStatsResponseDTO> obtenerEstadisticas(
            @PathVariable Integer id_usuario
    ) {
        return ResponseEntity.ok(usuarioService.obtenerEstadisticas(id_usuario));
    }
}
