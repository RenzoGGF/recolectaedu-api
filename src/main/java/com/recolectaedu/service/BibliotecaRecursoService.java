package com.recolectaedu.service;

import com.recolectaedu.dto.request.BibliotecaItemCreateRequestDTO;
import com.recolectaedu.dto.response.BibliotecaItemResponseDTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecaRecurso;
import com.recolectaedu.model.Recurso;
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

    private BibliotecaItemResponseDTO toDto(BibliotecaRecurso bibliotecaRecurso) {
        return BibliotecaItemResponseDTO.builder()
                .id_biblioteca_recurso(bibliotecaRecurso.getId_biblioteca_recurso())
                .titulo_recurso(bibliotecaRecurso.getRecurso().getTitulo())
                .agregado_el(bibliotecaRecurso.getAgregado_el())
                .build();
    }

    @Transactional
    public BibliotecaItemResponseDTO guardarRecursoEnBiblioteca(BibliotecaItemCreateRequestDTO request) {
        Recurso recurso = recursoRepository.findById(request.id_recurso())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        Biblioteca biblioteca = bibliotecaRepository.findById(request.id_biblioteca())
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        BibliotecaRecurso bibliotecaRecurso = BibliotecaRecurso.builder()
                .biblioteca(biblioteca)
                .recurso(recurso)
                .agregado_el(LocalDateTime.now())
                .build();

        BibliotecaRecurso guardado = bibliotecaRecursoRepository.save(bibliotecaRecurso);

        return toDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<BibliotecaItemResponseDTO> listarRecursos(Integer id_biblioteca) {
        Biblioteca biblioteca = bibliotecaRepository.findById(id_biblioteca)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
        return bibliotecaRecursoRepository.findByBiblioteca(biblioteca)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void eliminarRecurso(Integer id_biblioteca, Integer id_recurso) {
        Biblioteca biblioteca = bibliotecaRepository.findById(id_biblioteca)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        BibliotecaRecurso bibliotecaRecurso = bibliotecaRecursoRepository.findByBibliotecaAndRecurso(biblioteca, recurso)
                        .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado en la biblioteca"));

        bibliotecaRecursoRepository.delete(bibliotecaRecurso);
    }


}
