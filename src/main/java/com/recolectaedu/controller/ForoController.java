package com.recolectaedu.controller;

import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.service.ForoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/foros")
@RequiredArgsConstructor
public class ForoController {

    private final ForoService foroService;

    @PostMapping
    public ResponseEntity<ForoResponseDTO> crearTema(@Valid @RequestBody ForoRequestDTO foroRequest){
    ForoResponseDTO response = foroService.crearTema(foroRequest);
        return ResponseEntity.created(URI.create("/foros/" + response.getId_foro())).body(response);}
}