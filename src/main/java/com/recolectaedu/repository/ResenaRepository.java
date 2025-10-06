package com.recolectaedu.repository;

import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Resena;
import com.recolectaedu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    List<Resena> findByRecurso(Recurso recurso);
    List<Resena> findByUsuario(Usuario usuario);

    boolean existsByUsuarioAndRecurso(Usuario usuario, Recurso recurso);
}