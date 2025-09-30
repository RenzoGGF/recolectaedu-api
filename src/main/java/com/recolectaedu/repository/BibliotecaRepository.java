package com.recolectaedu.repository;

import com.recolectaedu.model.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Integer> {
    @Query("SELECT b FROM Biblioteca b WHERE b.usuario.id_usuario = :id_usuario")
    Optional<Biblioteca> findBibliotecaByUsuarioId(Integer id_usuario);
}