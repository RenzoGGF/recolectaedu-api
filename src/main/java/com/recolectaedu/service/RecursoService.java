package com.recolectaedu.service;

import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.AporteListadoResponseDTO;
import com.recolectaedu.dto.response.RecursoResponseDTO;
import com.recolectaedu.dto.response.RecursoValoradoResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.Periodo;
import com.recolectaedu.model.enums.Tipo_recurso;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    private RecursoResponseDTO toDto(Recurso r) {
        RecursoResponseDTO dto = new RecursoResponseDTO();
        dto.setId_recurso(r.getId_recurso());
        dto.setTitulo(r.getTitulo());
        dto.setDescripcion(r.getDescripcion());
        dto.setContenido(r.getContenido());
        dto.setFormato(r.getFormato());
        dto.setTipo(r.getTipo());
        dto.setCreado_el(r.getCreado_el());
        dto.setId_usuario(r.getUsuario() != null ? r.getUsuario().getId_usuario() : null);
        dto.setId_curso(r.getCurso() != null ? r.getCurso().getId_curso() : null);
        String autorNombre = "Anónimo";
        if (r.getUsuario() != null && r.getUsuario().getPerfil() != null) {
            autorNombre = r.getUsuario().getPerfil().getNombre();
        }
        dto.setAutorNombre(autorNombre);
        return dto;
    }

    private Periodo mapPeriodoOrdinal(Integer ordinal) {
        if (ordinal == null) return null;
        var valores = Periodo.values();
        if (ordinal < 0 || ordinal >= valores.length)
            throw new BusinessRuleException("Periodo inválido (valor fuera de rango)");
        return valores[ordinal];
    }

    // US-12
    public List<RecursoResponseDTO> findRecientesByCurso(Integer cursoId) {
        if (!cursoRepository.existsById(cursoId)) {
            throw new ResourceNotFoundException("El curso con ID " + cursoId + " no fue encontrado.");
        }

        List<Recurso> recursos = recursoRepository.findRecursosRecientesPorCurso(cursoId);

        return getRecursoResponseDTOS(recursos);
    }

    // US - 9 y 10
    public List<RecursoResponseDTO> searchRecursos(String keyword, Integer cursoId, String tipo, String autor, String universidad, Integer calificacionMinima, String ordenarPor) {
        Tipo_recurso tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = Tipo_recurso.valueOf(tipo);
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

        return getRecursoResponseDTOS(recursos);
    }

    @Transactional(readOnly = true)
    public List<RecursoValoradoResponseDTO> obtenerRecursosMasValoradosPorCurso(Integer id_curso) {
        Curso curso = cursoRepository.findById(id_curso)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        return recursoRepository.findMasValoradosPorCursoConMetricas(id_curso);
    }

    private List<RecursoResponseDTO> getRecursoResponseDTOS(List<Recurso> recursos) {
        return recursos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public RecursoResponseDTO crear(RecursoCreateRequestDTO req) {
        Usuario usuario = usuarioRepository.findById(req.id_usuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Curso curso = cursoRepository.findById(req.id_curso())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        Recurso recurso = Recurso.builder()
                .titulo(req.titulo())
                .descripcion(req.descripcion())
                .contenido(req.contenido())
                .formato(req.formato())
                .tipo(req.tipo())
                .ano(req.ano())
                .periodo(mapPeriodoOrdinal(req.periodo()))
                .usuario(usuario)
                .curso(curso)
                .creado_el(LocalDateTime.now())
                .actualizado_el(LocalDateTime.now())
                .build();

        return toDto(recursoRepository.save(recurso));
    }

    @Transactional
    public RecursoResponseDTO actualizar(Integer id_recurso, RecursoUpdateRequestDTO request) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        Curso curso = cursoRepository.findById(request.id_curso())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        recurso.setTitulo(request.titulo());
        recurso.setDescripcion(request.descripcion());
        recurso.setContenido(request.contenido());
        recurso.setFormato(request.formato());
        recurso.setTipo(request.tipo());
        recurso.setAno(request.ano());
        recurso.setPeriodo(mapPeriodoOrdinal(request.periodo()));
        recurso.setCurso(curso);
        recurso.setActualizado_el(LocalDateTime.now());

        return toDto(recursoRepository.save(recurso));
    }

    @Transactional
    public RecursoResponseDTO actualizarParcial(Integer id_recurso, RecursoPartialUpdateRequestDTO request) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        if (request.id_curso() != null) {
            Curso curso = cursoRepository.findById(request.id_curso())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
            recurso.setCurso(curso);
        }

        if (request.titulo() != null) recurso.setTitulo(request.titulo());
        if (request.descripcion() != null) recurso.setDescripcion(request.descripcion());
        if (request.contenido() != null) recurso.setContenido(request.contenido());
        if (request.formato() != null) recurso.setFormato(request.formato());
        if (request.tipo() != null) recurso.setTipo(request.tipo());
        if (request.ano() != null) recurso.setAno(request.ano());
        if (request.periodo() != null) recurso.setPeriodo(mapPeriodoOrdinal(request.periodo()));

        recurso.setActualizado_el(LocalDateTime.now());
        return toDto(recursoRepository.save(recurso));
    }

    @Transactional
    public void eliminar(Integer id_recurso) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        recursoRepository.delete(recurso);
    }

    // US-08: Historial de aportes del usuario autenticado
    @Transactional(readOnly = true)
    public Page<AporteListadoResponseDTO> listarMisAportes(
            Integer usuarioId,
            Integer cursoId,
            String tipo,
            Pageable pageable
    ) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId);
        }

        Tipo_recurso tipoEnum = null;
        if (tipo != null && !tipo.trim().isEmpty()) {
            final String tipoBusqueda = tipo;
            tipoEnum = Arrays.stream(Tipo_recurso.values())
                    .filter(tr -> tr.name().equalsIgnoreCase(tipoBusqueda))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El tipo de recurso '" + tipoBusqueda + "' no es válido."));
        }

        return recursoRepository.findAportesByUsuario(
                usuarioId,
                cursoId,
                tipoEnum,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public RecursoResponseDTO obtenerPorId(Integer id_recurso) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        return toDto(recurso);
    }
}
