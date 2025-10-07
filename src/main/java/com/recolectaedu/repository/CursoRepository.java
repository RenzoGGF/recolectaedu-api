package com.recolectaedu.repository;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponseDTO;
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
    @Query("SELECT new com.recolectaedu.dto.response.CursoResponseDTO(" +
            "c.id_curso, c.universidad, c.nombre, c.carrera, COUNT(r)) " +
            "FROM Recurso r JOIN r.curso c " +
            "GROUP BY c.id_curso, c.universidad, c.nombre, c.carrera " +
            "ORDER BY COUNT(r) DESC")
    List<CursoResponseDTO> findCursosPopulares();

    @Query("SELECT new com.recolectaedu.dto.response.CursoRankingAportesDTO(" +
            "c.id, c.nombre, c.universidad, c.carrera, COUNT(r)) " +
            "FROM Curso c LEFT JOIN Recurso r ON r.curso = c " +
            "WHERE (:universidad IS NULL OR c.universidad = :universidad) " +
            "AND (:carrera IS NULL OR c.carrera = :carrera) " +
            "GROUP BY c.id, c.nombre, c.universidad, c.carrera " +
            "ORDER BY COUNT(r) DESC")
    Page<CursoRankingAportesDTO> rankingPorAportes(@Param("universidad") String universidad, @Param("carrera") String carrera, Pageable pageable);

    Optional<Curso> findByUniversidadAndCarreraAndNombre(String universidad, String carrera, String nombre);

}