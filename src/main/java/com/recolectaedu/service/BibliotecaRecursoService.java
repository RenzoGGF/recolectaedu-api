package com.recolectaedu.service;

import com.recolectaedu.dto.request.BibliotecaRecursoRequestDTO;
import com.recolectaedu.dto.response.BibliotecaRecursoResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecaRecurso;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.BibliotecaRecursoRepository;
import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BibliotecaRecursoService {
    private final RecursoRepository recursoRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final BibliotecaRecursoRepository bibliotecaRecursoRepository;
    private final UsuarioService usuarioService;

    private BibliotecaRecursoResponseDTO toDto(BibliotecaRecurso bibliotecaRecurso) {
        return BibliotecaRecursoResponseDTO.builder()
                .id_biblioteca_recurso(bibliotecaRecurso.getId_biblioteca_recurso())
                .titulo_recurso(bibliotecaRecurso.getRecurso().getTitulo())
                .id_recurso(bibliotecaRecurso.getRecurso().getId_recurso())
                .agregado_el(bibliotecaRecurso.getAgregado_el())
                .build();
    }

    @Transactional
    public BibliotecaRecursoResponseDTO guardarRecursoEnBiblioteca(Integer id_biblioteca, BibliotecaRecursoRequestDTO request) {
        if (request == null || request.id_recurso() == null) {
            throw new IllegalArgumentException("El id del recurso es obligatorio");
        }

        Usuario usuario = usuarioService.getAuthenticatedUsuario();

        Recurso recurso = recursoRepository.findById(request.id_recurso())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        Biblioteca biblioteca = bibliotecaRepository.findById(id_biblioteca)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        validateOwnership(biblioteca, usuario);

        // Verificar duplicidad
        if (bibliotecaRecursoRepository.existsByBibliotecaAndRecurso(biblioteca, recurso)) {
            throw new BusinessRuleException("No pueden existir duplicados en la biblioteca");
        }

        BibliotecaRecurso bibliotecaRecurso = BibliotecaRecurso.builder()
                .biblioteca(biblioteca)
                .recurso(recurso)
                .agregado_el(LocalDateTime.now())
                .build();

        BibliotecaRecurso guardado = bibliotecaRecursoRepository.save(bibliotecaRecurso);

        return toDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<BibliotecaRecursoResponseDTO> listarRecursos(Integer id_biblioteca) {
        Usuario usuario = usuarioService.getAuthenticatedUsuario();

        Biblioteca biblioteca = bibliotecaRepository.findById(id_biblioteca)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        validateOwnership(biblioteca, usuario);

        return bibliotecaRecursoRepository.findByBiblioteca(biblioteca)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void eliminarRecurso(Integer id_biblioteca, Integer id_recurso) {
        Usuario usuario = usuarioService.getAuthenticatedUsuario();

        Biblioteca biblioteca = bibliotecaRepository.findById(id_biblioteca)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        validateOwnership(biblioteca, usuario);

        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        BibliotecaRecurso bibliotecaRecurso = bibliotecaRecursoRepository.findByBibliotecaAndRecurso(biblioteca, recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado en la biblioteca"));

        bibliotecaRecursoRepository.delete(bibliotecaRecurso);
    }

    private void validateOwnership(Biblioteca biblioteca, Usuario auth) {
        if (!biblioteca.getUsuario().getId_usuario().equals(auth.getId_usuario())) {
            throw new BusinessRuleException("No autorizado para operar sobre esta biblioteca");
        }
    }

    @Transactional(readOnly = true)
    public boolean verificarRecursoEnBiblioteca(Integer idBiblioteca, Integer idRecurso) {
        return bibliotecaRecursoRepository.existsByBiblioteca_Id_bibliotecaAndRecurso_Id_recurso(idBiblioteca, idRecurso);
    }
}
