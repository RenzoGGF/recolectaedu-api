package com.recolectaedu.repository;

import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Resena;
import com.recolectaedu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    // US - 17 Total de reseñas recibidas por los recursos del autor
    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id_usuario = :userId")
    long countResenasRecibidasPorAutor(@Param("userId") Integer userId);

    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id_usuario = :userId and rs.es_positivo = true")
    long countResenasPositivasPorAutor(@Param("userId") Integer userId);

    @Query("select count(rs) from Resena rs where rs.recurso.usuario.id_usuario = :userId and rs.es_positivo = false")
    long countResenasNegativasPorAutor(@Param("userId") Integer userId);
  
    List<Resena> findByRecurso(Recurso recurso);
    List<Resena> findByUsuario(Usuario usuario);

    boolean existsByUsuarioAndRecurso(Usuario usuario, Recurso recurso);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.recurso.id_recurso = :recursoId AND r.es_positivo = :esPositivo")
    long countByRecurso_Id_recursoAndEsPositivo(@Param("recursoId") Integer recursoId, @Param("esPositivo") boolean esPositivo);
}