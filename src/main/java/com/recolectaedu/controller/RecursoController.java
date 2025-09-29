package com.recolectaedu.controller;

import com.recolectaedu.service.RecursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recursos")
public class RecursoController {

    @Autowired
    private RecursoService recursoService;

}