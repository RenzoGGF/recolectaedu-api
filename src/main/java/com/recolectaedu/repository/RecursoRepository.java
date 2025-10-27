package com.recolectaedu.repository;

import com.recolectaedu.dto.response.AporteListadoResponseDTO;
import com.recolectaedu.dto.response.RecursoValoradoResponseDTO;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface    RecursoRepository extends JpaRepository<Recurso, Integer> {

    // US-12: Recursos recientes por curso
    @Query("SELECT r FROM Recurso r WHERE r.curso.id_curso = :cursoId ORDER BY r.creado_el DESC")
    List<Recurso> findRecursosRecientesPorCurso(@Param("cursoId") Integer cursoId);


    // US-9 y 10: Búsqueda Avanzada
    @Query("SELECT r, " +
            "SUM(CASE WHEN res.es_positivo = true THEN 1 WHEN res.es_positivo = false THEN -1 ELSE 0 END) as score " +
            "FROM Recurso r " +
            "LEFT JOIN r.curso c " +
            "LEFT JOIN r.usuario u " +
            "LEFT JOIN u.perfil p " +
            "LEFT JOIN Resena res ON res.recurso = r " +
            "WHERE " +
            "(LOWER(CAST(r.titulo AS STRING)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(CAST(r.descripcion AS STRING)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR :keyword IS NULL) AND " +
            "(:cursoId IS NULL OR c.id_curso = :cursoId) AND " +
            "(:tipoEnum IS NULL OR r.tipo = :tipoEnum) AND " +
            "(LOWER(CAST(p.nombre AS STRING)) LIKE LOWER(CONCAT('%', :autorNombre, '%')) " +
            "OR LOWER(CAST(p.apellidos AS STRING)) LIKE LOWER(CONCAT('%', :autorNombre, '%')) OR :autorNombre IS NULL) AND " +
            "(LOWER(CAST(c.universidad AS STRING)) LIKE LOWER(CONCAT('%', :universidad, '%')) OR :universidad IS NULL) " +
            "GROUP BY r.id_recurso, r.titulo, r.descripcion, r.ano, r.periodo, r.creado_el, " +
            "r.actualizado_el, r.contenido, r.formato, r.tipo, r.usuario, r.curso " +
            "HAVING :calificacionMinima IS NULL OR " +
            "SUM(CASE WHEN res.es_positivo = true THEN 1 WHEN res.es_positivo = false THEN -1 ELSE 0 END) >= :calificacionMinima")
    List<Object[]> search(
            @Param("keyword") String keyword,
            @Param("cursoId") Integer cursoId,
            @Param("tipoEnum") Tipo_recurso tipoEnum,
            @Param("autorNombre") String autorNombre,
            @Param("universidad") String universidad,
            @Param("calificacionMinima") Integer calificacionMinima,
            Sort sort
    );


    // US-17 - Recursos publicados por el usuario (autor)
    @Query("select count(r) from Recurso r where r.usuario.id_usuario = :userId")
    long countByAutorId(Integer userId);
  
    // Ordena los recursos según su valoración neta de un curso
    @Query("""
        select new com.recolectaedu.dto.response.RecursoValoradoResponseDTO(
            r.id_recurso,
            r.titulo,
            r.descripcion,
            r.ano,
            cast(r.periodo as integer),
            cast(coalesce(sum(case when rs.es_positivo = true then 1 else 0 end), 0) as integer),
            cast(coalesce(sum(case when rs.es_positivo = false then 1 else 0 end), 0) as integer),
            cast((coalesce(sum(case when rs.es_positivo = true then 1 else 0 end), 0) -
                  coalesce(sum(case when rs.es_positivo = false then 1 else 0 end), 0)) as integer),
            r.actualizado_el
        )
        from Recurso r
        left join Resena rs on rs.recurso = r
        where r.curso.id_curso = :idCurso
        group by r.id_recurso, r.titulo, r.descripcion, r.ano, r.periodo, r.actualizado_el
        order by (coalesce(sum(case when rs.es_positivo = true then 1 else 0 end), 0) -
                  coalesce(sum(case when rs.es_positivo = false then 1 else 0 end), 0)) desc,
                 r.actualizado_el desc
        """)
    List<RecursoValoradoResponseDTO> findMasValoradosPorCursoConMetricas(Integer idCurso);

    // US-08: Historial de aportes del usuario autenticado
    @Query("""
        SELECT new com.recolectaedu.dto.response.AporteListadoResponseDTO(
            r.id_recurso,
            r.titulo,
            r.tipo,
            r.curso.id_curso,
            r.curso.nombre,
            r.curso.universidad,
            r.creado_el,
            r.actualizado_el
        )
        FROM Recurso r
        WHERE r.usuario.id_usuario = :usuarioId
        AND (:cursoId IS NULL OR r.curso.id_curso = :cursoId)
        AND (:tipo IS NULL OR r.tipo = :tipo)
        """)
    Page<AporteListadoResponseDTO> findAportesByUsuario(
        @Param("usuarioId") Integer usuarioId,
        @Param("cursoId") Integer cursoId,
        @Param("tipo") Tipo_recurso tipo,
        Pageable pageable
    );
}
