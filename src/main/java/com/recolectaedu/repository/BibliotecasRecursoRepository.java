package com.recolectaedu.repository;

import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecasRecurso;
import com.recolectaedu.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BibliotecasRecursoRepository extends JpaRepository<BibliotecasRecurso, Integer> {
    boolean existsByBibliotecaAndRecurso(Biblioteca biblioteca, Recurso recurso);
    List<BibliotecasRecurso> findByBiblioteca(Biblioteca biblioteca);
    Optional<BibliotecasRecurso> findByBibliotecaAndRecurso(Biblioteca biblioteca, Recurso recurso);
}