package com.recolectaedu.controller;

import com.recolectaedu.dto.response.BibliotecaResponseDTO;
import com.recolectaedu.service.BibliotecaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/usuarios/{id_usuario}/biblioteca")
@RequiredArgsConstructor
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    @GetMapping
    public ResponseEntity<BibliotecaResponseDTO> obtenerBibliotecaPorUsuario(
            @PathVariable Integer id_usuario
    ) {
        return ResponseEntity.ok(bibliotecaService.obtenerBibliotecaPorUsuarioId(id_usuario));
    }

    @PostMapping
    public ResponseEntity<BibliotecaResponseDTO> crearBiblioteca(
            @PathVariable Integer id_usuario
    ) {
        return ResponseEntity.created(URI.create("/usuarios/" + id_usuario + "/biblioteca"))
                .body(bibliotecaService.crearBiblioteca(id_usuario));
    }
}