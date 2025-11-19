package com.recolectaedu.controller;

import com.recolectaedu.dto.request.RecursoArchivoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.RecursoResponseDTO;
import com.recolectaedu.service.RecursoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

    @Operation(summary = "Actualizar recurso (Solo metadatos/texto)")
    @PutMapping(value = "/{id_recurso}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecursoResponseDTO> actualizar(
            @PathVariable Integer id_recurso,
            @Valid @RequestBody RecursoUpdateRequestDTO request) {
        return ResponseEntity.ok(recursoService.actualizar(id_recurso, request));
    }

    @Operation(summary = "Actualizar recurso (Reemplazar archivo y metadatos)")
    @PutMapping(value = "/{id_recurso}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecursoResponseDTO> actualizarConArchivo(
            @PathVariable Integer id_recurso,
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("metadata") RecursoArchivoCreateRequestDTO metadata
    ) {
        return ResponseEntity.ok(recursoService.actualizarDesdeArchivo(id_recurso, archivo, metadata));
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

    @Operation(summary = "Descargar archivo asociado al recurso")
    @GetMapping("/{id_recurso}/archivo")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable Integer id_recurso
    ) {
        Resource archivo = recursoService.obtenerArchivo(id_recurso);

        String contentType = "application/octet-stream"; // Tipo genérico binario

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getFilename() + "\"")
                .body(archivo);
    }
}