package com.recolectaedu.service;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.PerfilRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    public Usuario registrarUsuario(UserRequestDTO userRequestDTO) {

        return null;
    }

    public Usuario obtenerUsuarioPorId(Integer id) {

        return null;
    }

    public Usuario actualizarPerfil(Integer id, PerfilRequestDTO perfilRequestDTO) {
        return null;
    }

    public void eliminarUsuario(Integer id) {
    }
}