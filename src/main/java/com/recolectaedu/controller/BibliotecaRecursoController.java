package com.recolectaedu.controller;

import com.recolectaedu.dto.request.BibliotecaRecursoRequestDTO;
import com.recolectaedu.dto.response.BibliotecaRecursoResponseDTO;
import com.recolectaedu.service.BibliotecaRecursoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            @Valid @RequestBody BibliotecaRecursoRequestDTO request
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

    @Operation(summary = "Verificar si un recurso está guardado en una biblioteca específica")
    @GetMapping("/{id_recurso}/verificar")
    public ResponseEntity<Map<String, Boolean>> verificarExistencia(
            @PathVariable Integer id_biblioteca,
            @PathVariable Integer id_recurso
    ) {
        boolean existe = bibliotecaRecursoService.verificarRecursoEnBiblioteca(id_biblioteca, id_recurso);

        // JSON simple{ "guardado": true }
        return ResponseEntity.ok(Map.of("guardado", existe));
    }
}
