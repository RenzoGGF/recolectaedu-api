package com.recolectaedu.controller;

import com.recolectaedu.service.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comentarios") // O podría anidarse bajo /foros/{foroId}/comentarios
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

}