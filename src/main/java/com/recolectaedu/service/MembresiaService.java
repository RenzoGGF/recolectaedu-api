package com.recolectaedu.service;

import com.recolectaedu.dto.request.MembresiaRequestDTO;
import com.recolectaedu.dto.response.MembresiaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Membresia;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.repository.MembresiaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final UsuarioRepository usuarioRepository;
    private final MembresiaRepository membresiaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public MembresiaResponseDTO create(Integer idUsuario, MembresiaRequestDTO req) {
        Usuario user = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPropietarioOAdmin(user);

        // no más de una membresia ACTIVE vigente por plan
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

        syncUserRole(user); // sincroniza el rolTipo del usuario

        return toDTO(m);
    }

    @Transactional
    public MembresiaResponseDTO cancelActive(Integer idUsuario) {
        // 1) Obtener usuario objetivo
        Usuario user = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2) Validar que el autenticado sea el mismo (o ADMIN)
        validarPropietarioOAdmin(user);

        // 3) Buscar membresías activas vigentes
        var actives = membresiaRepository.findAllActiveNowOrderByStartsDesc(idUsuario, Instant.now());
        if (actives.isEmpty()) {
            throw new BusinessRuleException("El usuario no tiene una membresía activa vigente");
        }

        var m = actives.getFirst();

        // 4) Cancelar la membresía
        m.setAutoRenew(false);
        m.setEndsAt(Instant.now());
        m.setStatus(MembresiaStatus.CANCELED);
        m = membresiaRepository.save(m);

        syncUserRole(user);

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

        // Sincronizar rolTipo por cada usuario
        for (Integer uid : usuariosAfectados) {
            usuarioRepository.findById(uid).ifPresent(this::syncUserRole);
        }
        return vencidas.size();
    }

    @Transactional(readOnly = true)
    public List<MembresiaResponseDTO> list(Integer idUsuario) {
        var user = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPropietarioOAdmin(user);

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
        RolTipo desired = active ? RolTipo.ROLE_PREMIUM : RolTipo.ROLE_FREE;
        if (user.getRolTipo() != desired) {
            user.setRolTipo(desired);
            usuarioRepository.save(user); // persiste el cambio de rolTipo
        }
    }

    // helper validador propietario o admin
    private void validarPropietarioOAdmin(Usuario objetivo) {
        Usuario auth = usuarioService.getAuthenticatedUsuario();

        if (auth.getRolTipo() == RolTipo.ROLE_ADMIN) {
            return;
        }
        if (!auth.getId_usuario().equals(objetivo.getId_usuario())) {
            throw new AccessDeniedException("No puedes operar sobre membresías de otro usuario");
        }
    }

}
