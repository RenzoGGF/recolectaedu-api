package com.recolectaedu.controller;

import com.recolectaedu.dto.request.RecursoArchivoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.RecursoResponseDTO;
import com.recolectaedu.service.RecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService recursoService;

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