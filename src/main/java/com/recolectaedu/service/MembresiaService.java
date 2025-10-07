package com.recolectaedu.service;

import com.recolectaedu.dto.request.MembresiaRequestDTO;
import com.recolectaedu.dto.response.MembresiaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Membresia;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.Rol;
import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.repository.MembresiaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final UsuarioRepository usuarioRepository;
    private final MembresiaRepository membresiaRepository;

    @Transactional
    public MembresiaResponseDTO create(Integer idUsuario, MembresiaRequestDTO req) {
        Usuario user = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // no más de una membresia ACTIVE vigente por plan
        // MembresiaService.create(...)
        if (membresiaRepository.hasAnyActiveNow(idUsuario, Instant.now())) {
            throw new BusinessRuleException("Ya existe una membresía activa vigente para este usuario");
        }


        Instant starts = Instant.now();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime endsUtc = switch (req.plan()) {
            case MONTHLY -> nowUtc.plusMonths(1);
            case ANNUAL  -> nowUtc.plusYears(1);
        };
        Instant ends = endsUtc.toInstant();

        Membresia m = Membresia.builder()
                .usuario(user)
                .plan(req.plan())
                .status(MembresiaStatus.ACTIVE) // simulacion pago exitoso
                .autoRenew(req.autoRenew())
                .startsAt(starts)
                .endsAt(ends)
                .build();

        m = membresiaRepository.save(m);

        syncUserRole(user); // sincroniza el rol del usuario

        return toDTO(m);
    }

    @Transactional
    public MembresiaResponseDTO cancelActive(Integer idUsuario) {
        var actives = membresiaRepository.findAllActiveNowOrderByStartsDesc(idUsuario, Instant.now());
        if (actives.isEmpty()) {
            throw new BusinessRuleException("El usuario no tiene una membresía activa vigente");
        }

        var m = actives.getFirst();

        // desactivar autoRenew, cerrar vigencia y marcar estado
        m.setAutoRenew(false);
        m.setEndsAt(Instant.now());
        m.setStatus(MembresiaStatus.CANCELED);
        m = membresiaRepository.save(m);

        // Sincroniza rol del usuario (pasar a FREE)
        return toDTO(m);
    }

    @Transactional
    public int expireOverdue() {
        Instant now = Instant.now();
        // ACTIVE cuyo endsAt ya expiró
        var vencidas = membresiaRepository.findByStatusAndEndsAtBefore(MembresiaStatus.ACTIVE, now);
        if (vencidas.isEmpty()) return 0;

        // Pasar a EXPIRED y recolectar usuarios afectados
        Set<Integer> usuariosAfectados = new HashSet<>();
        for (Membresia m : vencidas) {
            m.setStatus(MembresiaStatus.EXPIRED);
            usuariosAfectados.add(m.getUsuario().getId_usuario());
        }
        membresiaRepository.saveAll(vencidas);

        // Sincronizar rol por cada usuario
        for (Integer uid : usuariosAfectados) {
            usuarioRepository.findById(uid).ifPresent(this::syncUserRole);
        }
        return vencidas.size();
    }

    @Transactional(readOnly = true)
    public List<MembresiaResponseDTO> list(Integer idUsuario) {
        // asegura que el usuario existe
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return membresiaRepository.findAllByUsuarioOrderByStartsDesc(idUsuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Helpers
    private MembresiaResponseDTO toDTO(Membresia m) {
        return new MembresiaResponseDTO(
                m.getId(),
                m.getPlan(),
                m.getStatus(),
                m.isAutoRenew(),
                m.getStartsAt(),
                m.getEndsAt()
        );
    }

    private void syncUserRole(Usuario user) {
        boolean active = membresiaRepository.hasActiveMembership(user.getId_usuario(), Instant.now());
        Rol desired = active ? Rol.PREMIUM : Rol.FREE;
        if (user.getRol() != desired) {
            user.setRol(desired);
            usuarioRepository.save(user); // persiste el cambio de rol
        }
    }
}
