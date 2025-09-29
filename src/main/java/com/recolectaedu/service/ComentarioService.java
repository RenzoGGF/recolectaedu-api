package com.recolectaedu.service;

import com.recolectaedu.repository.ComentarioRepository;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ForoRepository foroRepository;

}