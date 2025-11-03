package com.recolectaedu.service;

import com.recolectaedu.dto.request.RecursoArchivoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.model.enums.OrdenRecurso;
import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.AporteConContadoresResponseDTO;
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
import com.recolectaedu.repository.ComentarioRepository;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.recolectaedu.dto.response.RecursoResponse2DTO;


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
    private final IAlmacenamientoService almacenamientoService;
    private final ResenaRepository resenaRepository;
    private final ComentarioRepository comentarioRepository;

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

    private RecursoResponse2DTO mapToRecursoResponse2DTO(Recurso recurso) {
        String autorNombre = "Anónimo";
        if (recurso.getUsuario() != null && recurso.getUsuario().getPerfil() != null) {
            autorNombre = recurso.getUsuario().getPerfil().getNombre();
        }

        return new RecursoResponse2DTO(
                recurso.getId_recurso(),
                recurso.getTitulo(),
                recurso.getDescripcion(),
                recurso.getContenido(),
                recurso.getFormato(),
                recurso.getTipo(),
                recurso.getCreado_el(),
                recurso.getUsuario() != null ? recurso.getUsuario().getId_usuario() : null,
                recurso.getCurso() != null ? recurso.getCurso().getId_curso() : null,
                autorNombre
        );
    }

    private Periodo mapPeriodoOrdinal(Integer ordinal) {
        if (ordinal == null) return null;
        var valores = Periodo.values();
        if (ordinal < 0 || ordinal >= valores.length)
            throw new BusinessRuleException("Periodo inválido. Valores permitidos: 0 (verano), 1 (primer), 2 (segundo).");
        return valores[ordinal];
    }

    private Curso validarYObtenerCurso(String universidad, String carrera, String nombreCurso) {
        return cursoRepository.findByUniversidadAndCarreraAndNombre(universidad, carrera, nombreCurso)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("La clasificación académica es inválida. No se encontró el curso '%s' para la carrera '%s' en la universidad '%s'.",
                                nombreCurso, carrera, universidad)));
    }

    // US-12
    public List<RecursoResponse2DTO> findRecientesByCurso(Integer cursoId) {
        if (!cursoRepository.existsById(cursoId)) {
            throw new ResourceNotFoundException("El curso con ID " + cursoId + " no fue encontrado.");
        }

        List<Recurso> recursos = recursoRepository.findRecursosRecientesPorCurso(cursoId);

        return recursos.stream()
                .map(this::mapToRecursoResponse2DTO)
                .collect(Collectors.toList());
    }

    // US - 9 y 10
    public List<RecursoResponse2DTO> searchRecursos(String keyword, Integer cursoId, String tipo, String autor, String universidad, Integer calificacionMinima, OrdenRecurso ordenarPor) {
        Tipo_recurso tipoEnum = null;
        if (tipo != null && !tipo.trim().isEmpty()) {
            final String tipoBusqueda = tipo;
            tipoEnum = Arrays.stream(Tipo_recurso.values())
                    .filter(tr -> tr.name().equalsIgnoreCase(tipoBusqueda))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El tipo de recurso '" + tipoBusqueda + "' no es válido."));
        }
        List<Object[]> resultados = recursoRepository.search(
                keyword,
                cursoId,
                tipoEnum,
                autor,
                universidad,
                calificacionMinima,
                Sort.unsorted()
        );
        if (ordenarPor == OrdenRecurso.RELEVANTES) {
            resultados.sort((o1, o2) -> {
                try {
                    Long score1 = ((Number) o1[1]).longValue();
                    Long score2 = ((Number) o2[1]).longValue();
                    return Long.compare(score2, score1);
                } catch (Exception e) {
                    return 0;
                }
            });
        } else {
            resultados.sort((o1, o2) -> ((Recurso) o2[0]).getCreado_el().compareTo(((Recurso) o1[0]).getCreado_el()));
        }
        return resultados.stream()
                .map(resultado -> {
                    Recurso recurso = (Recurso) resultado[0];
                    return mapToRecursoResponse2DTO(recurso);
                })
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + req.id_usuario()));

        Curso curso = validarYObtenerCurso(req.universidad(), req.carrera(), req.nombreCurso());

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
    public RecursoResponseDTO crearDesdeArchivo(MultipartFile archivo, RecursoArchivoCreateRequestDTO req) {
        String nombreArchivo = almacenamientoService.almacenar(archivo);

        Usuario usuario = usuarioRepository.findById(req.id_usuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + req.id_usuario()));

        Curso curso = validarYObtenerCurso(req.universidad(), req.carrera(), req.nombreCurso());

        Recurso recurso = Recurso.builder()
                .titulo(req.titulo())
                .descripcion(req.descripcion())
                .contenido(nombreArchivo) // Guardamos el path/nombre del archivo
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

        Curso curso = validarYObtenerCurso(request.universidad(), request.carrera(), request.nombreCurso());

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

        // Si se quiere actualizar la clasificación, se deben proporcionar los tres campos.
        if (request.universidad() != null && request.carrera() != null && request.nombreCurso() != null) {
            Curso curso = validarYObtenerCurso(request.universidad(), request.carrera(), request.nombreCurso());
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
        // Opcional: eliminar el archivo físico si el recurso es de tipo archivo
        if (recurso.getFormato() == com.recolectaedu.model.enums.FormatoRecurso.ARCHIVO) {
            almacenamientoService.eliminar(recurso.getContenido());
        }
        recursoRepository.delete(recurso);
    }

    // US-08: Historial de aportes del usuario autenticado
    @Transactional(readOnly = true)
    public Page<AporteConContadoresResponseDTO> listarMisAportes(
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

        Page<AporteListadoResponseDTO> aportes = recursoRepository.findAportesByUsuario(
                usuarioId,
                cursoId,
                tipoEnum,
                pageable
        );

        return aportes.map(aporte -> {
            int votosPositivos = (int) resenaRepository.countByRecurso_Id_recursoAndEsPositivo(aporte.getId(), true);
            int votosNegativos = (int) resenaRepository.countByRecurso_Id_recursoAndEsPositivo(aporte.getId(), false);
            // int comentarios = (int) comentarioRepository.countByRecursoId(aporte.getId()); // Not possible yet
            return new AporteConContadoresResponseDTO(
                    aporte.getId(),
                    aporte.getTitulo(),
                    aporte.getTipo(),
                    aporte.getCursoId(),
                    aporte.getCursoNombre(),
                    aporte.getUniversidad(),
                    aporte.getFechaCreacion(),
                    aporte.getFechaActualizacion(),
                    votosPositivos,
                    votosNegativos,
                    0 // Hardcoded to 0
            );
        });
    }

    @Transactional(readOnly = true)
    public RecursoResponseDTO obtenerPorId(Integer id_recurso) {
        Recurso recurso = recursoRepository.findById(id_recurso)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        return toDto(recurso);
    }
}
