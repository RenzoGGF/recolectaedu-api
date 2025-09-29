package com.recolectaedu.service;

import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForoService {

    @Autowired
    private ForoRepository foroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


}