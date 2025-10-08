package com.recolectaedu.repository;

import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Integer> {
    Optional<Biblioteca> findByUsuario(Usuario usuario);
}