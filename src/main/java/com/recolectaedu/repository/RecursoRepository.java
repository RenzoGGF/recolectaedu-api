package com.recolectaedu.repository;

import com.recolectaedu.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecursoRepository extends JpaRepository<Recurso, Integer> {
}