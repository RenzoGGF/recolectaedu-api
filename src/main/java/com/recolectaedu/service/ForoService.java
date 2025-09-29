package com.recolectaedu.service;

import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForoService {

    private final ForoRepository foroRepository;
    private final UsuarioRepository usuarioRepository;

}