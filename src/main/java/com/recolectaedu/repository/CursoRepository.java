package com.recolectaedu.repository;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.dto.response.UniversidadCursoCountDTO;
import com.recolectaedu.dto.response.UniversidadRankingRecursosDTO;
import com.recolectaedu.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Integer> {


    // US-11: Cursos Populares
    @Query("SELECT new com.recolectaedu.dto.response.CursoResponse2DTO(" +
            "c.id_curso, c.universidad, c.nombre, c.carrera, COUNT(r)) " +
            "FROM Recurso r JOIN r.curso c " +
            "WHERE c.universidad = :institucion " +
            "GROUP BY c.id_curso, c.universidad, c.nombre, c.carrera " +
            "ORDER BY COUNT(r) DESC")
    List<CursoResponse2DTO> findCursosPopulares(@Param("institucion") String institucion);

    @Query("SELECT new com.recolectaedu.dto.response.CursoRankingAportesDTO(" +
            "c.id_curso, c.nombre, c.universidad, c.carrera, (COUNT(DISTINCT r.id_recurso) + COUNT(DISTINCT res.id_resena))) " +
            "FROM Curso c " +
            "LEFT JOIN Recurso r ON c = r.curso " +
            "LEFT JOIN Resena res ON r = res.recurso " +
            "WHERE (:universidad IS NULL OR c.universidad = :universidad) " +
            "AND (:carrera IS NULL OR c.carrera = :carrera) " +
            "GROUP BY c.id_curso, c.nombre, c.universidad, c.carrera " +
            "ORDER BY (COUNT(DISTINCT r.id_recurso) + COUNT(DISTINCT res.id_resena)) DESC, c.nombre ASC")
    Page<CursoRankingAportesDTO> rankingPorAportes(@Param("universidad") String universidad, @Param("carrera") String carrera, Pageable pageable);
    Optional<Curso> findByUniversidadAndCarreraAndNombre(String universidad, String carrera, String nombre);

    @Query("SELECT new com.recolectaedu.dto.response.CursoResponse2DTO(" +
            "c.id_curso, c.universidad, c.nombre, c.carrera, COUNT(r)) " +
            "FROM Curso c LEFT JOIN Recurso r ON c = r.curso " +
            "WHERE c.id_curso = :idCurso " +
            "GROUP BY c.id_curso, c.universidad, c.nombre, c.carrera")
    Optional<CursoResponse2DTO> findCursoDetailsById(@Param("idCurso") Integer idCurso);

    @Query("SELECT new com.recolectaedu.dto.response.UniversidadRankingRecursosDTO(" +
            "c.universidad, COUNT(r.id_recurso)) " +
            "FROM Recurso r JOIN r.curso c " +
            "GROUP BY c.universidad " +
            "ORDER BY COUNT(r.id_recurso) DESC")
    List<UniversidadRankingRecursosDTO> findRankingUniversidadesPorRecursos();

    @Query("SELECT new com.recolectaedu.dto.response.UniversidadCursoCountDTO(c.universidad, COUNT(c)) " +
            "FROM Curso c GROUP BY c.universidad")
    List<UniversidadCursoCountDTO> obtenerUniversidadesConConteo();

}