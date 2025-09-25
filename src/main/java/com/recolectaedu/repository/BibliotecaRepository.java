package com.recolectaedu.repository;

import com.recolectaedu.model.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Integer> {
}