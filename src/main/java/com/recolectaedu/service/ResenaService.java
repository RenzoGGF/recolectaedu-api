package com.recolectaedu.service;

import com.recolectaedu.dto.request.ResenaRequestCreateDTO;
import com.recolectaedu.dto.request.ResenaRequestUpdateDTO;
import com.recolectaedu.dto.response.ResenaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Resena;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoRepository recursoRepository;

    private ResenaResponseDTO toDto(Resena resena) {
        return ResenaResponseDTO.builder()
                .id_resena(resena.getId_resena())
                .contenido(resena.getContenido())
                .es_positivo(resena.getEs_positivo())
                .nombre_autor(resena.getUsuario().getPerfil().getNombre())
                .titulo_recurso(resena.getRecurso().getTitulo())
                .creado_el(resena.getCreado_el())
                .actualizado_el(resena.getActualizado_el())
                .build();
    }

    @Transactional
    public ResenaResponseDTO crearResena(ResenaRequestCreateDTO request) {
        Usuario usuario = usuarioRepository.findById(request.id_usuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Recurso recurso = recursoRepository.findById(request.id_recurso())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        if (resenaRepository.existsByUsuarioAndRecurso(usuario, recurso))
            throw new BusinessRuleException("El usuario ya ha dado una reseña al recurso");

        Resena resena = Resena.builder()
                .contenido(request.contenido())
                .es_positivo(request.es_positivo())
                .usuario(usuario)
                .recurso(recurso)
                .creado_el(LocalDateTime.now())
                .actualizado_el(LocalDateTime.now())
                .build();

        return toDto(resenaRepository.save(resena));
    }

    @Transactional(readOnly = true)
    public ResenaResponseDTO obtenerResena(Integer id_resena) {
        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        return toDto(resena);
    }

    @Transactional
    public ResenaResponseDTO actualizarResena(Integer id_resena, ResenaRequestUpdateDTO request) {
        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        // No permite modificar contenido que no es propio del usuario
        if (!resena.getUsuario().getId_usuario().equals(request.id_usuario()))
            throw new BusinessRuleException("No se puede modificar la reseña de otro usuario.");

        resena.setContenido(request.contenido());
        resena.setEs_positivo(request.es_positivo());
        resena.setActualizado_el(LocalDateTime.now());

        return toDto(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(Integer id_resena) {
        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        resenaRepository.delete(resena);
    }

    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorRecurso(Integer id_recurso) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        return resenaRepository.findByRecurso(recurso).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorUsuario(Integer id_usuario) {
        Usuario usuario = usuarioRepository.findById(id_usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return resenaRepository.findByUsuario(usuario).stream().map(this::toDto).toList();
    }
}