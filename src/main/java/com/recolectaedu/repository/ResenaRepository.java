package com.recolectaedu.repository;

import com.recolectaedu.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    // US - 17 Total de reseñas recibidas por los recursos del autor
    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id = :userId")
    long countResenasRecibidasPorAutor(@Param("userId") Integer userId);

    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id = :userId and rs.es_positivo = true")
    long countResenasPositivasPorAutor(@Param("userId") Integer userId);

    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id = :userId and rs.es_positivo = false")
    long countResenasNegativasPorAutor(@Param("userId") Integer userId);
}