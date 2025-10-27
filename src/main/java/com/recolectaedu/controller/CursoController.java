package com.recolectaedu.controller;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    //US-11 - Listar cursos más populares
    @GetMapping("/populares")
    public ResponseEntity<List<CursoResponse2DTO>> findCursosPopulares(
            @RequestParam String institucion
    ) {
        List<CursoResponse2DTO> response = cursoService.findCursosPopulares(institucion);
        return ResponseEntity.ok(response);
    }

    //US18 - Ranking de cursos con mas aportes
    @GetMapping("/ranking-aportes")
    public ResponseEntity<Page<CursoRankingAportesDTO>> getRankingAportes(
            @RequestParam(required = false) String universidad,
            @RequestParam(required = false) String carrera,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CursoRankingAportesDTO> response = cursoService.getRankingAportes(universidad, carrera, pageable);
        return ResponseEntity.ok(response);
    }
}