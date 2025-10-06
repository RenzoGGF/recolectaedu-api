package com.recolectaedu.repository;

import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.enums.Tipo_recurso;
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
    @Query("SELECT r FROM Recurso r " +
            "LEFT JOIN r.curso c " +
            "LEFT JOIN r.usuario u " +
            "LEFT JOIN u.perfil p " +
            "LEFT JOIN Resena res ON res.recurso = r " +
            "WHERE " +
            "(:keyword IS NULL OR LOWER(CAST(r.titulo AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(r.descripcion AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:cursoId IS NULL OR c.id_curso = :cursoId) AND " +
            "(:tipoEnum IS NULL OR r.tipo = :tipoEnum) AND " +
            "(:autorNombre IS NULL OR LOWER(CAST(p.nombre AS string)) LIKE LOWER(CONCAT('%', :autorNombre, '%')) OR LOWER(CAST(p.apellidos AS string)) LIKE LOWER(CONCAT('%', :autorNombre, '%'))) AND " +
            "(:universidad IS NULL OR LOWER(CAST(c.universidad AS string)) LIKE LOWER(CONCAT('%', :universidad, '%'))) " +
            "GROUP BY r.id_recurso " +
            "HAVING :calificacionMinima IS NULL OR " +
            "SUM(CASE WHEN res.es_positivo = true THEN 1 WHEN res.es_positivo = false THEN -1 ELSE 0 END) >= :calificacionMinima")
    List<Recurso> search(
            @Param("keyword") String keyword,
            @Param("cursoId") Integer cursoId,
            @Param("tipoEnum") Tipo_recurso tipoEnum,
            @Param("autorNombre") String autorNombre,
            @Param("universidad") String universidad,
            @Param("calificacionMinima") Integer calificacionMinima,
            Sort sort
    );

    // US-17 - Recursos publicados por el usuario (autor)
    @Query("select count(r) from Recurso r where r.usuario.id = :userId")
    long countByAutorId(Integer userId);
}

