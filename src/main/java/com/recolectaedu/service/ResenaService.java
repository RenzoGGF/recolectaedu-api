package com.recolectaedu.service;

import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoRepository recursoRepository;

}