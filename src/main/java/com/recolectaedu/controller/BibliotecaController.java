package com.recolectaedu.controller;

import com.recolectaedu.dto.response.BibliotecaResponseDTO;
import com.recolectaedu.service.BibliotecaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/usuarios/biblioteca")
@RequiredArgsConstructor
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    @GetMapping
    public ResponseEntity<BibliotecaResponseDTO> obtenerBibliotecaPorUsuario() {
        return ResponseEntity.ok(bibliotecaService.obtenerBIbliotecaDeUsuario());
    }

    @PostMapping
    public ResponseEntity<BibliotecaResponseDTO> crearBiblioteca() {
        BibliotecaResponseDTO dto = bibliotecaService.crearBiblioteca();
        URI location = URI.create("/usuarios/" + dto.id_usuario() + "/biblioteca");
        return ResponseEntity.created(location).body(dto);
    }
}