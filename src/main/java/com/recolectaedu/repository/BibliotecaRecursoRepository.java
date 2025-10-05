package com.recolectaedu.repository;

import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecaRecurso;
import com.recolectaedu.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BibliotecaRecursoRepository extends JpaRepository<BibliotecaRecurso, Integer> {
    boolean existsByBibliotecaAndRecurso(Biblioteca biblioteca, Recurso recurso);
    List<BibliotecaRecurso> findByBiblioteca(Biblioteca biblioteca);
    Optional<BibliotecaRecurso> findByBibliotecaAndRecurso(Biblioteca biblioteca, Recurso recurso);
}