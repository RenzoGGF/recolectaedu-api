package com.recolectaedu.controller;

import com.recolectaedu.dto.response.RecursoResponse;
import com.recolectaedu.service.RecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService recursoService;

    // Endpoint para US-12
    @GetMapping("/curso/{cursoId}/recientes")
    public ResponseEntity<List<RecursoResponse>> findRecientesByCurso(@PathVariable Integer cursoId) {
        List<RecursoResponse> response = recursoService.findRecientesByCurso(cursoId);
        return ResponseEntity.ok(response);
    }
    // Endpoint para US-9 y US-10
    @GetMapping
    public ResponseEntity<List<RecursoResponse>> searchRecursos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer cursoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String universidad,
            @RequestParam(required = false) Integer calificacionMinima,
            @RequestParam(required = false) String ordenarPor
    ) {
        List<RecursoResponse> response = recursoService.searchRecursos(keyword, cursoId, tipo, autor, universidad, calificacionMinima, ordenarPor);
        return ResponseEntity.ok(response);
    }

}