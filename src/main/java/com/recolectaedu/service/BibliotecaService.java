package com.recolectaedu.service;

import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.BibliotecasRecursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BibliotecaService {

    @Autowired
    private BibliotecaRepository bibliotecaRepository;

    @Autowired
    private BibliotecasRecursoRepository bibliotecasRecursoRepository;

    @Autowired
    private RecursoRepository recursoRepository;

}