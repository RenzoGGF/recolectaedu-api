package com.recolectaedu.controller;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Por hacer: devolver un ResponseDTO sin password_hash
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    //POST /usuarios/register
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO req) {
        var resp = usuarioService.registrarUsuario(req); // que devuelva UserResponseDTO
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // GET /usuarios/{id}
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Integer id) {
        var resp = usuarioService.obtenerUsuarioPorIdDTO(id);
        return ResponseEntity.ok(resp); // 200
    }

    // PUT /usuarios/{id}/profile
    @PutMapping(value = "/{id}/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> upsertProfile(
            @PathVariable Integer id,
            @Valid @RequestBody PerfilRequestDTO body) {
        var resp = usuarioService.actualizarPerfilDTO(id, body);
        return ResponseEntity.ok(resp); // 200
    }

    // DELETE /usuarios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
