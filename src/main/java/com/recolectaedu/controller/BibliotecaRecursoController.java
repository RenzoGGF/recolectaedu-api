package com.recolectaedu.controller;

import com.recolectaedu.dto.request.BibliotecaRecursoCreateRequestDTO;
import com.recolectaedu.dto.response.BibliotecaRecursoResponseDTO;
import com.recolectaedu.service.BibliotecaRecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bibliotecas/{id_biblioteca}/recursos")
@RequiredArgsConstructor
public class BibliotecaRecursoController {

    private final BibliotecaRecursoService bibliotecaRecursoService;

    @GetMapping
    public ResponseEntity<List<BibliotecaRecursoResponseDTO>> listarRecursos(
            @PathVariable Integer id_biblioteca
    ) {
        return ResponseEntity.ok(bibliotecaRecursoService.listarRecursos(id_biblioteca));
    }

    @PostMapping
    public ResponseEntity<BibliotecaRecursoResponseDTO> guardarRecursoEnBiblioteca(
            @PathVariable Integer id_biblioteca,
            @Valid @RequestBody BibliotecaRecursoCreateRequestDTO request
    ) {
        return ResponseEntity.ok(bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request));
    }

    @DeleteMapping("/{id_recurso}")
    public ResponseEntity<Void> eliminarPorRecurso(
            @PathVariable Integer id_biblioteca,
            @PathVariable Integer id_recurso
    ) {
        bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso);
        return ResponseEntity.noContent().build();
    }
}
