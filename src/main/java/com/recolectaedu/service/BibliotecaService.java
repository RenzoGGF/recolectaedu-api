package com.recolectaedu.service;

import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.BibliotecasRecursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BibliotecaService {

    private final BibliotecaRepository bibliotecaRepository;
    private final BibliotecasRecursoRepository bibliotecasRecursoRepository;
    private final RecursoRepository recursoRepository;

}