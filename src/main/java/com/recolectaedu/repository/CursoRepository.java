package com.recolectaedu.repository;

import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Integer> {


    // US-11: Cursos Populares
    @Query("SELECT new com.recolectaedu.dto.response.CursoResponseDTO(" +
            "c.id_curso, c.universidad, c.nombre, c.carrera, COUNT(r)) " +
            "FROM Recurso r JOIN r.curso c " +
            "GROUP BY c.id_curso, c.universidad, c.nombre, c.carrera " +
            "ORDER BY COUNT(r) DESC")
    List<CursoResponseDTO> findCursosPopulares();

}