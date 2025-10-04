package com.recolectaedu.service;

import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Foro;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForoService {

    private final ForoRepository foroRepository;
    private final UsuarioRepository usuarioRepository;

    public ForoResponseDTO crearTema(ForoRequestDTO foroRequest){

        Usuario usuario = usuarioRepository.findById(foroRequest.getId_usuario())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + foroRequest.getId_usuario()));

        Foro nuevoForo = new Foro();
        nuevoForo.setTitulo(foroRequest.getTitulo());
        nuevoForo.setContenido(foroRequest.getContenido());
        nuevoForo.setUsuario(usuario);

        Foro foroGuardado = foroRepository.save(nuevoForo);

        return new ForoResponseDTO(
                foroGuardado.getId_foro(),
                foroGuardado.getTitulo(),
                foroGuardado.getCreado_el(),
                foroGuardado.getUsuario().getId_usuario()
        );

    }
}