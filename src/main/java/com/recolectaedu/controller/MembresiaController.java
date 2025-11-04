package com.recolectaedu.controller;

import com.recolectaedu.dto.request.MembresiaRequestDTO;
import com.recolectaedu.dto.response.MembresiaResponseDTO;
import com.recolectaedu.service.MembresiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios/{idUsuario}/membresias")
@RequiredArgsConstructor
public class MembresiaController {

    private final MembresiaService membresiaService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MembresiaResponseDTO> create(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody MembresiaRequestDTO req) {
        var resp = membresiaService.create(idUsuario, req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MembresiaResponseDTO>> list(@PathVariable Integer idUsuario) {
        var resp = membresiaService.list(idUsuario);
        return ResponseEntity.ok(resp);
    }

    // Cancelar membresia en la cuenta logueada
    @PutMapping(value = "/cancelar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MembresiaResponseDTO> cancelActive(
            @PathVariable Integer idUsuario) {
        var resp = membresiaService.cancelActive(idUsuario);
        return ResponseEntity.ok(resp);
    }

    // Cancelar una membresia por id
    @PutMapping(value = "/{idMembresia}/cancelar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MembresiaResponseDTO> cancel(
            @PathVariable Integer idUsuario) {
        var resp = membresiaService.cancelActive(idUsuario);
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}
