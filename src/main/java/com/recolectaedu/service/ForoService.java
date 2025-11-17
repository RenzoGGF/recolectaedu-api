package com.recolectaedu.service;

import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Foro;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForoService {

    private final ForoRepository foroRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public ForoResponseDTO crearTema(ForoRequestDTO foroRequest){
        Usuario usuarioActual = usuarioService.getAuthenticatedUsuario(); //
        Foro nuevoForo = new Foro();
        nuevoForo.setTitulo(foroRequest.titulo());
        nuevoForo.setContenido(foroRequest.contenido());
        nuevoForo.setUsuario(usuarioActual);
        Foro foroGuardado = foroRepository.save(nuevoForo);
        return new ForoResponseDTO(
                foroGuardado.getId_foro(),
                foroGuardado.getTitulo(),
                foroGuardado.getContenido(),
                foroGuardado.getCreado_el(),
                foroGuardado.getUsuario().getId_usuario(),
                foroGuardado.getUsuario().getPerfil().getNombre(),
                foroGuardado.getUsuario().getPerfil().getApellidos()
        );
    }

    public List<ForoResponseDTO> listarTodosLosForos() {
        return foroRepository.findAllForosDTO();
    }

    public ForoResponseDTO getForoById(Integer idForo) {
        return foroRepository.findForoDetailsById(idForo)
                .orElseThrow(() -> new EntityNotFoundException("Foro no encontrado con id: " + idForo));
    }
}