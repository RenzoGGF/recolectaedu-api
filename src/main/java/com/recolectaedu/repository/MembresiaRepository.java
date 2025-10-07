package com.recolectaedu.repository;

import com.recolectaedu.model.Membresia;
import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.model.enums.Plan;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.*;

public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    @Query("""
       SELECT (COUNT(m) > 0)
       FROM Membresia m
       WHERE m.usuario.id_usuario = :idUsuario
         AND m.status = com.recolectaedu.model.enums.MembresiaStatus.ACTIVE
         AND m.startsAt < :now AND m.endsAt > :now
       """)
    boolean hasAnyActiveNow(@Param("idUsuario") Integer idUsuario,
                            @Param("now") java.time.Instant now);

    @Query("""
       SELECT m
       FROM Membresia m
       WHERE m.usuario.id_usuario = :idUsuario
         AND m.status = com.recolectaedu.model.enums.MembresiaStatus.ACTIVE
         AND m.startsAt < :now AND m.endsAt > :now
       ORDER BY m.startsAt DESC
       """)
    java.util.List<Membresia> findAllActiveNowOrderByStartsDesc(@Param("idUsuario") Integer idUsuario,
                                                                @Param("now") java.time.Instant now);

    @Query("""
       SELECT m
       FROM Membresia m
       WHERE m.usuario.id_usuario = :idUsuario
         AND m.status = com.recolectaedu.model.enums.MembresiaStatus.ACTIVE
         AND m.startsAt < :now AND m.endsAt > :now
       """)
    Optional<Membresia> findActiveNowByUsuario(@Param("idUsuario") Integer idUsuario,
                                               @Param("now") Instant now);

    @Query("""
           SELECT (COUNT(m) > 0)
           FROM Membresia m
           WHERE m.usuario.id_usuario = :idUsuario
             AND m.status = com.recolectaedu.model.enums.MembresiaStatus.ACTIVE
             AND m.startsAt < :now AND m.endsAt > :now
           """)
    boolean hasActiveMembership(@Param("idUsuario") Integer idUsuario,
                                @Param("now") Instant now);

    @Query("""
           SELECT m
           FROM Membresia m
           WHERE m.usuario.id_usuario = :idUsuario
           ORDER BY m.startsAt DESC
           """)
    List<Membresia> findAllByUsuarioOrderByStartsDesc(@Param("idUsuario") Integer idUsuario);

    // 🔹 Para expirar en batch: no hay nested, se puede método derivado
    List<Membresia> findByStatusAndEndsAtBefore(MembresiaStatus status, Instant now);
}
