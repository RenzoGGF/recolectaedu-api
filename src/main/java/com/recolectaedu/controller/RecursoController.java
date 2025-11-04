package com.recolectaedu.controller;

import com.recolectaedu.dto.request.RecursoArchivoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.RecursoResponse2DTO;
import com.recolectaedu.dto.response.RecursoResponseDTO;
import com.recolectaedu.dto.response.RecursoValoradoResponseDTO;
import com.recolectaedu.service.RecursoService;
import jakarta.validation.Valid;
import com.recolectaedu.model.enums.OrdenRecurso;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService recursoService;

    // Endpoint para US-12
    @GetMapping("/curso/{cursoId}/recientes")
    public ResponseEntity<List<RecursoResponse2DTO>> findRecientesByCurso(@PathVariable Integer cursoId) {
        List<RecursoResponse2DTO> response = recursoService.findRecientesByCurso(cursoId);
        return ResponseEntity.ok(response);
    }
    // Endpoint para US-9 y US-10
    @GetMapping
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

    @GetMapping("/curso/{id_curso}/mas-valorados")
    public ResponseEntity<List<RecursoValoradoResponseDTO>> obtenerMasValoradosPorCurso(
            @PathVariable("id_curso") Integer id_curso
    ) {
        return ResponseEntity.ok(recursoService.obtenerRecursosMasValoradosPorCurso(id_curso));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecursoResponseDTO> crearRecursoJson(
            @Valid @RequestBody RecursoCreateRequestDTO request
    ) {
        RecursoResponseDTO creado = recursoService.crear(request);
        return ResponseEntity.created(URI.create("/recursos/" + creado.getId_recurso())).body(creado);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RecursoResponseDTO> crearRecursoArchivo(
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("metadata") RecursoArchivoCreateRequestDTO metadata
    ) {
        RecursoResponseDTO creado = recursoService.crearDesdeArchivo(archivo, metadata);
        return ResponseEntity.created(URI.create("/recursos/" + creado.getId_recurso())).body(creado);
    }

    @GetMapping("/{id_recurso}")
    public ResponseEntity<RecursoResponseDTO> obtener(
            @PathVariable
            Integer id_recurso) {
        return ResponseEntity.ok(recursoService.obtenerPorId(id_recurso));
    }

    @PutMapping("/{id_recurso}")
    public ResponseEntity<RecursoResponseDTO> actualizar(
            @PathVariable Integer id_recurso,
            @Valid @RequestBody RecursoUpdateRequestDTO request) {
        return ResponseEntity.ok(recursoService.actualizar(id_recurso, request));
    }

    @PatchMapping("/{id_recurso}")
    public ResponseEntity<RecursoResponseDTO> actualizarParcial(
            @PathVariable Integer id_recurso,
            @RequestBody RecursoPartialUpdateRequestDTO request) {
        return ResponseEntity.ok(recursoService.actualizarParcial(id_recurso, request));
    }

    @DeleteMapping("/{id_recurso}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id_recurso
    ) {
        recursoService.eliminar(id_recurso);
        return ResponseEntity.noContent().build();
    }
}