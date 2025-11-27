package com.recolectaedu.service;

import com.recolectaedu.dto.request.ComentarioRequestDTO;
import com.recolectaedu.dto.response.ComentarioResponseDTO;
import com.recolectaedu.model.Comentario;
import com.recolectaedu.model.Foro;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.ComentarioRepository;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ForoRepository foroRepository;

    @Transactional
    public ComentarioResponseDTO crearComentario(ComentarioRequestDTO request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Foro foro = foroRepository.findById(request.id_foro())
                .orElseThrow(() -> new EntityNotFoundException("Foro no encontrado"));

        Comentario padre = null;
        if (request.id_comentario_padre() != null) {
            padre = comentarioRepository.findById(request.id_comentario_padre())
                    .orElseThrow(() -> new EntityNotFoundException("Comentario padre no encontrado"));
        }

        Comentario comentario = Comentario.builder()
                .contenido(request.contenido())
                .usuario(usuario)
                .foro(foro)
                .comentario_padre(padre)
                .build();

        Comentario guardado = comentarioRepository.save(comentario);

        return mapToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarComentariosPorForo(Integer idForo) {
        List<Comentario> comentarios = comentarioRepository.buscarComentariosPorForo(idForo);
        return comentarios.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ComentarioResponseDTO mapToDTO(Comentario c) {
        return new ComentarioResponseDTO(
                c.getId_comentario(),
                c.getContenido(),
                c.getCreado_el(),
                c.getUsuario().getPerfil().getNombre(),
                c.getUsuario().getPerfil().getApellidos(),
                c.getUsuario().getId_usuario(),
                c.getComentario_padre() != null ? c.getComentario_padre().getId_comentario() : null
        );
    }
}