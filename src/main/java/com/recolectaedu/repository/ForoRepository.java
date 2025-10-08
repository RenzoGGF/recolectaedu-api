package com.recolectaedu.repository;

import com.recolectaedu.model.Foro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForoRepository extends JpaRepository<Foro, Integer> {
}