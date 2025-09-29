package com.recolectaedu.service;

import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

}