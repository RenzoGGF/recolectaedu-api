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
import lombok.RequiredArgsConstructor;
import com.recolectaedu.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        return toDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<BibliotecaItemResponseDTO> listarRecursos(Integer userId) {
        var biblioteca = bibliotecaRepository.findBibliotecaByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
        return bibliotecasRecursoRepository.findByBiblioteca(biblioteca)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Para eliminar un item de la biblioteca cuando se tiene el idBibliotecaRecurso
    // en el listado de la biblioteca
    @Transactional
    public void eliminarItem(Integer idUsuario, Integer idBibliotecaRecurso) {
        var biblioteca = bibliotecaRepository.findBibliotecaByUsuarioId(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        var br = bibliotecasRecursoRepository.findById(idBibliotecaRecurso)
                .orElseThrow(() -> new ResourceNotFoundException("Item de biblioteca no encontrado"));

        // Solo se pueden eliminar items de una biblioteca propia
        if (!br.getBiblioteca().getId_biblioteca().equals(biblioteca.getId_biblioteca())) {
            throw new BusinessRuleException("No puedes eliminar items de otra biblioteca");
        }

        bibliotecasRecursoRepository.delete(br);
    }

    // Para eliminar un item de la biblioteca con el id del recurso y el usuario
    @Transactional
    public void eliminarPorRecurso(Integer idUsuario, Integer idRecurso) {
        var biblioteca = bibliotecaRepository.findBibliotecaByUsuarioId(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        var recurso = recursoRepository.findById(idRecurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        var brOpt = bibliotecasRecursoRepository.findByBibliotecaAndRecurso(biblioteca, recurso);
        if (brOpt.isEmpty()) {
            throw new ResourceNotFoundException("El recurso no está en la biblioteca");
        }
        bibliotecasRecursoRepository.delete(brOpt.get());
    }

    private BibliotecaItemResponseDTO toDto(BibliotecasRecurso br) {
        String agregadoEl = br.getAgregado_el() != null
                ? br.getAgregado_el().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;
        return BibliotecaItemResponseDTO.builder()
                .id_biblioteca_recurso(br.getId_biblioteca_recurso())
                .id_recurso(br.getRecurso().getId_recurso())
                .titulo_recurso(br.getRecurso().getTitulo())
                .agregado_el(agregadoEl)
                .build();
    }
}