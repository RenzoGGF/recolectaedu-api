package com.recolectaedu.controller;

import com.recolectaedu.service.ForoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foros")
public class ForoController {

    @Autowired
    private ForoService foroService;


}