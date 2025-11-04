package com.recolectaedu.controller;

import com.recolectaedu.dto.request.ResenaRequestCreateDTO;
import com.recolectaedu.dto.request.ResenaRequestUpdateDTO;
import com.recolectaedu.dto.response.ResenaResponseDTO;
import com.recolectaedu.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/resenas")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ResenaController {

    private final ResenaService resenaService;

    @GetMapping("/{id_resena}")
    public ResponseEntity<ResenaResponseDTO> obtenerResena(
            @PathVariable Integer id_resena
    ){
        return ResponseEntity.ok(resenaService.obtenerResena(id_resena));
    }

    @GetMapping("/recurso/{id_recurso}")
    public ResponseEntity<List<ResenaResponseDTO>> listarPorRecurso(
            @PathVariable Integer id_recurso
    ) {
        return ResponseEntity.ok(resenaService.listarPorRecurso(id_recurso));
    }

    @GetMapping("/usuario/{id_usuario}")
    public ResponseEntity<List<ResenaResponseDTO>> listarPorUsuario(
            @PathVariable Integer id_usuario
    ) {
        return ResponseEntity.ok(resenaService.listarPorUsuario(id_usuario));
    }

    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crearResena(
            @Valid @RequestBody ResenaRequestCreateDTO request
    ) {
        ResenaResponseDTO creada = resenaService.crearResena(request);
        return ResponseEntity.created(URI.create("/resenas/" + creada.id_resena())).body(creada);
    }

    @PutMapping("/{id_resena}")
    public ResponseEntity<ResenaResponseDTO> actualizarResena(
            @PathVariable Integer id_resena,
            @Valid @RequestBody ResenaRequestUpdateDTO request
    ) {
        return ResponseEntity.ok(resenaService.actualizarResena(id_resena, request));
    }

    @DeleteMapping("/{id_resena}")
    public ResponseEntity<Void> eliminarResena(
            @PathVariable Integer id_resena
    ) {
        resenaService.eliminar(id_resena);
        return ResponseEntity.noContent().build();
    }
}