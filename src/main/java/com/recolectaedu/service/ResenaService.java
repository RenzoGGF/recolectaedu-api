package com.recolectaedu.service;

import com.recolectaedu.dto.request.ResenaRequestCreateDTO;
import com.recolectaedu.dto.request.ResenaRequestPartialUpdateDTO;
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
import jakarta.validation.Valid;
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
    private final UsuarioService usuarioService;
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
    public ResenaResponseDTO crearResena(@Valid ResenaRequestCreateDTO request) {
        if (request.contenido() == null || request.contenido().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        if (request.es_positivo() == null) {
            throw new IllegalArgumentException("El valor del voto no puede estar vacío");
        }

        Usuario usuarioActual = usuarioService.getAuthenticatedUsuario();

        Recurso recurso = recursoRepository.findById(request.id_recurso())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        if (resenaRepository.existsByUsuarioAndRecurso(usuarioActual, recurso))
            throw new BusinessRuleException("El usuario ya ha dado una reseña al recurso");

        Resena resena = Resena.builder()
                .contenido(request.contenido())
                .es_positivo(request.es_positivo())
                .usuario(usuarioActual)
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
        Usuario usuarioActual = usuarioService.getAuthenticatedUsuario();

        if (request.contenido() == null || request.contenido().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        if (request.es_positivo() == null) {
            throw new IllegalArgumentException("El valor del voto no puede estar vacío");
        }

        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        validateOwnership(resena, usuarioActual);

        resena.setContenido(request.contenido());
        resena.setEs_positivo(request.es_positivo());
        resena.setActualizado_el(LocalDateTime.now());

        return toDto(resenaRepository.save(resena));
    }

    @Transactional
    public ResenaResponseDTO actualizarParcialResena(Integer id_resena, ResenaRequestPartialUpdateDTO request) {
        Usuario usuarioActual = usuarioService.getAuthenticatedUsuario();

        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        validateOwnership(resena, usuarioActual);

        validateOwnership(resena, usuarioActual);

        if (request.contenido() != null) {
            resena.setContenido(request.contenido());
        }
        if (request.es_positivo() != null) {
            resena.setEs_positivo(request.es_positivo());
        }
        resena.setActualizado_el(LocalDateTime.now());

        return toDto(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(Integer id_resena) {
        Usuario usuarioActual = usuarioService.getAuthenticatedUsuario();

        Resena resena = resenaRepository.findById(id_resena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        validateOwnership(resena, usuarioActual);

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

    private void validateOwnership(Resena resena, Usuario usuarioActual) {
        if (!resena.getUsuario().getId_usuario().equals(usuarioActual.getId_usuario())) {
            throw new BusinessRuleException("No tienes permiso para operar sobre esta reseña");
        }
    }
}