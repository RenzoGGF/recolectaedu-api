package com.recolectaedu.repository;

import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.model.Foro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForoRepository extends JpaRepository<Foro, Integer> {

    @Query("SELECT new com.recolectaedu.dto.response.ForoResponseDTO(" +
            "f.id_foro, f.titulo, f.contenido, f.creado_el, u.id_usuario, p.nombre, p.apellidos) " +
            "FROM Foro f JOIN f.usuario u JOIN u.perfil p " +
            "ORDER BY f.creado_el DESC")
    List<ForoResponseDTO> findAllForosDTO();

    @Query("SELECT new com.recolectaedu.dto.response.ForoResponseDTO(" +
            "f.id_foro, f.titulo,f.contenido, f.creado_el, u.id_usuario, p.nombre, p.apellidos) " +
            "FROM Foro f JOIN f.usuario u JOIN u.perfil p " +
            "WHERE f.id_foro = :idForo")
    Optional<ForoResponseDTO> findForoDetailsById(@Param("idForo") Integer idForo);
}