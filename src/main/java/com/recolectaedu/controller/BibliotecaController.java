package com.recolectaedu.controller;

import com.recolectaedu.dto.request.BibliotecaItemCreateRequestDTO;
import com.recolectaedu.dto.response.BibliotecaItemResponseDTO;
import com.recolectaedu.service.BibliotecaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios/{id_usuario}/biblioteca")
@RequiredArgsConstructor
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    @PostMapping("/items")
    public ResponseEntity<BibliotecaItemResponseDTO> crearItem(
            @PathVariable Integer id_usuario,
            @Valid @RequestBody BibliotecaItemCreateRequestDTO request) {

        BibliotecaItemResponseDTO resp = bibliotecaService.guardarRecursoEnBiblioteca(id_usuario, request);

        return ResponseEntity.created(
                        URI.create("/usuarios/" + id_usuario + "/biblioteca/items/" + resp.id_biblioteca_recurso()))
                .body(resp);
    }

    @GetMapping("/items")
    public ResponseEntity<List<BibliotecaItemResponseDTO>> listarItems(@PathVariable Integer id_usuario) {
        return ResponseEntity.ok(bibliotecaService.listarRecursos(id_usuario));
    }
}