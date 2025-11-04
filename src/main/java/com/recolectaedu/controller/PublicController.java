package com.recolectaedu.controller;

import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.dto.response.RecursoResponse2DTO;
import com.recolectaedu.dto.response.RecursoValoradoResponseDTO;
import com.recolectaedu.model.enums.OrdenRecurso;
import com.recolectaedu.service.CursoService;
import com.recolectaedu.service.RecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final RecursoService recursoService;
    private final CursoService cursoService;

    // Endpoint para US-12
    @GetMapping("/recursos/curso/{cursoId}/recientes")
    public ResponseEntity<List<RecursoResponse2DTO>> findRecientesByCurso(@PathVariable Integer cursoId) {
        List<RecursoResponse2DTO> response = recursoService.findRecientesByCurso(cursoId);
        return ResponseEntity.ok(response);
    }

    //US-11 - Listar cursos más populares
    @GetMapping("/cursos/populares")
    public ResponseEntity<List<CursoResponse2DTO>> findCursosPopulares(
            @RequestParam String institucion
    ) {
        List<CursoResponse2DTO> response = cursoService.findCursosPopulares(institucion);
        return ResponseEntity.ok(response);
    }

    // Endpoint para US-9 y US-10
    @GetMapping("/recursos")
    public ResponseEntity<List<RecursoResponse2DTO>> searchRecursos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer cursoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String universidad,
            @RequestParam(required = false) Integer calificacionMinima,
            @RequestParam(required = false) OrdenRecurso ordenarPor
    ) {
        List<RecursoResponse2DTO> response = recursoService.searchRecursos(keyword, cursoId, tipo, autor, universidad, calificacionMinima, ordenarPor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("recursos/curso/{id_curso}/mas-valorados")
    public ResponseEntity<List<RecursoValoradoResponseDTO>> obtenerMasValoradosPorCurso(
            @PathVariable("id_curso") Integer id_curso
    ) {
        return ResponseEntity.ok(recursoService.obtenerRecursosMasValoradosPorCurso(id_curso));
    }
}
