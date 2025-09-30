package com.recolectaedu.service;

import com.recolectaedu.dto.response.RecursoResponse;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;

    // US-12
    public List<RecursoResponse> findRecientesByCurso(Integer cursoId) {
        if (!cursoRepository.existsById(cursoId)) {
            throw new ResourceNotFoundException("El curso con ID " + cursoId + " no fue encontrado.");
        }

        List<Recurso> recursos = recursoRepository.findRecursosRecientesPorCurso(cursoId);

        return recursos.stream()
                .map(recurso -> {
                    RecursoResponse response = new RecursoResponse();
                    response.setId_recurso(recurso.getId_recurso());
                    response.setTitulo(recurso.getTitulo());
                    response.setDescripcion(recurso.getDescripcion());
                    response.setContenido(recurso.getContenido());
                    response.setFormato(recurso.getFormato());
                    response.setTipo(recurso.getTipo());
                    response.setCreado_el(recurso.getCreado_el());
                    response.setId_usuario(recurso.getUsuario().getId_usuario());
                    response.setId_curso(recurso.getCurso().getId_curso());

                    String autorNombre = "Anónimo";
                    if (recurso.getUsuario() != null && recurso.getUsuario().getPerfil() != null) {
                        autorNombre = recurso.getUsuario().getPerfil().getNombre();
                    }
                    response.setAutorNombre(autorNombre);

                    return response;
                }).collect(Collectors.toList());
    }

    // US - 9 y 10
    public List<RecursoResponse> searchRecursos(String keyword, Integer cursoId, String tipo, String autor, String universidad, Integer calificacionMinima, String ordenarPor) {
        Tipo_recurso tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = Tipo_recurso.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de recurso inválido: " + tipo);
            }
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "creado_el");

        if ("titulo".equalsIgnoreCase(ordenarPor)) {
            sort = Sort.by(Sort.Direction.ASC, "titulo");
        }

        List<Recurso> recursos = recursoRepository.search(
                keyword,
                cursoId,
                tipoEnum,
                autor,
                universidad,
                calificacionMinima,
                sort
        );

        return recursos.stream().map(recurso -> {
            RecursoResponse response = new RecursoResponse();
            response.setId_recurso(recurso.getId_recurso());
            response.setTitulo(recurso.getTitulo());
            response.setDescripcion(recurso.getDescripcion());
            response.setContenido(recurso.getContenido());
            response.setFormato(recurso.getFormato());
            response.setTipo(recurso.getTipo());
            response.setCreado_el(recurso.getCreado_el());
            response.setId_usuario(recurso.getUsuario().getId_usuario());
            response.setId_curso(recurso.getCurso().getId_curso());

            String autorNombre = "Anónimo";
            if (recurso.getUsuario() != null && recurso.getUsuario().getPerfil() != null) {
                autorNombre = recurso.getUsuario().getPerfil().getNombre();
            }
            response.setAutorNombre(autorNombre);

            return response;
        }).collect(Collectors.toList());
    }

}