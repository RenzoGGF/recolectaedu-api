package com.recolectaedu.service;

import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;

}