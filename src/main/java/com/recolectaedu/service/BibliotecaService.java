package com.recolectaedu.service;

import com.recolectaedu.dto.request.BibliotecaItemCreateRequestDTO;
import com.recolectaedu.dto.response.BibliotecaItemResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecasRecurso;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.BibliotecasRecursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.recolectaedu.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BibliotecaService {
    private final BibliotecaRepository bibliotecaRepository;
    private final BibliotecasRecursoRepository bibliotecasRecursoRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario buscarUsuario(Integer id_usuario) {
        return usuarioRepository.findById(id_usuario).orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
    }

    private Biblioteca obtenerOCrearBibliotecaUsuario(Integer id_usuario) {
        return bibliotecaRepository.findBibliotecaByUsuarioId(id_usuario)
                .orElseGet(() -> {
                    Usuario usuario = buscarUsuario(id_usuario);
                    Biblioteca b = new Biblioteca();
                    b.setNombre("Mi biblioteca");
                    b.setUsuario(usuario);
                    return bibliotecaRepository.save(b);
                });
    }

    @Transactional
    public BibliotecaItemResponseDTO guardarRecursoEnBiblioteca(Integer id_usuario, BibliotecaItemCreateRequestDTO request) {
        Biblioteca biblioteca = obtenerOCrearBibliotecaUsuario(id_usuario);

        Recurso recurso = recursoRepository.findById(request.id_recurso())
                .orElseThrow(() -> new ResourceNotFoundException("El recurso no existe."));

        // No pueden existir recursos duplicados en una biblioteca
        if (bibliotecasRecursoRepository.existsByBibliotecaAndRecurso(biblioteca, recurso)) {
            throw new BusinessRuleException("El recurso ya se encuentra en la biblioteca.");
        }

        BibliotecasRecurso guardado = bibliotecasRecursoRepository.save(
                BibliotecasRecurso.builder()
                        .biblioteca(biblioteca)
                        .recurso(recurso)
                        .build()
        );

        String agregado_el = guardado.getAgregado_el() != null
                ? guardado.getAgregado_el().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return BibliotecaItemResponseDTO.builder()
                .id_biblioteca_recurso(guardado.getId_biblioteca_recurso())
                .id_recurso(guardado.getRecurso().getId_recurso())
                .titulo_recurso(guardado.getRecurso().getTitulo())
                .agregado_el(agregado_el)
                .build();
    }

}