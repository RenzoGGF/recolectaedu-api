package com.recolectaedu.controller;

import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    //US-11 - Listar cursos más populares
    @GetMapping("/populares")
    public ResponseEntity<List<CursoResponseDTO>> findCursosPopulares() {
        List<CursoResponseDTO> response = cursoService.findCursosPopulares();
        return ResponseEntity.ok(response);
    }
}