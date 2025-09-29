package com.recolectaedu.controller;

import com.recolectaedu.service.ForoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/foros")
@RequiredArgsConstructor
public class ForoController {

    private final ForoService foroService;

}