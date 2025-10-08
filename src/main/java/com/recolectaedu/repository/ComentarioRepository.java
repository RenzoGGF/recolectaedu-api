package com.recolectaedu.repository;

import com.recolectaedu.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    // US17 - Comentarios HECHOS por el usuario
    @Query("select count(c) from Comentario c where c.usuario.id = :userId")
    long countByAutorId(Integer userId);
}