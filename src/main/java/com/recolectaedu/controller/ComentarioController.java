package com.recolectaedu.controller;

import com.recolectaedu.dto.request.ComentarioRequestDTO;
import com.recolectaedu.dto.response.ComentarioResponseDTO;
import com.recolectaedu.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> crearComentario(
            @Valid @RequestBody ComentarioRequestDTO request,
            Principal principal
    ) {
        ComentarioResponseDTO response = comentarioService.crearComentario(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}