package com.recolectaedu.controller;

import com.recolectaedu.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    // El método getRankingAportes ha sido movido a PublicController.
    // Este controlador se mantiene para futuros endpoints privados relacionados con cursos.

}
