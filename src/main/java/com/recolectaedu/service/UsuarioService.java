package com.recolectaedu.service;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.PerfilResponseDTO;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.dto.response.UsuarioStatsResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final RecursoRepository recursoRepository;
    private final ComentarioRepository comentarioRepository;
    private final ResenaRepository resenaRepository;
    private final BibliotecaRecursoRepository bibliotecaRecursoRepository;
  
    // POST
    @Transactional
    public UserResponseDTO registrarUsuario(UserRequestDTO r) {
        if (usuarioRepository.existsByEmail(r.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado");
        }

        Usuario u = Usuario.builder()
                .email(r.getEmail())
                .password_hash(passwordEncoder.encode(r.getPassword()))
                .rolTipo(resolveRol(r.getRol()))
                .build();

        if (r.getPerfil() != null) {
            var pr = r.getPerfil();
            Perfil p = Perfil.builder()
                    .nombre(pr.getNombre())
                    .apellidos(pr.getApellidos())
                    .universidad(pr.getUniversidad())
                    .carrera(pr.getCarrera())
                    .ciclo(pr.getCiclo())
                    .build();
            u.attachPerfil(p); // establece ambos lados (MapsId)
        }

        var saved = usuarioRepository.save(u);
        return toDTO(saved);
    }

    // GET
    @Transactional(readOnly = true)
    public UserResponseDTO obtenerUsuarioPorIdDTO(Integer id) {
        var user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPropietarioOAdmin(user);

        return toDTO(user);
    }

    // PUT
    @Transactional
    public UserResponseDTO actualizarPerfilDTO(Integer id, PerfilRequestDTO dto) {
        var user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPropietarioOAdmin(user);

        var perfil = user.getPerfil();
        if (perfil == null) {
            perfil = Perfil.builder().build();
            user.attachPerfil(perfil); // vincula ambos lados y comparte PK (MapsId)
        }

        perfil.setNombre(dto.getNombre());
        perfil.setApellidos(dto.getApellidos());
        perfil.setUniversidad(dto.getUniversidad());
        perfil.setCarrera(dto.getCarrera());
        perfil.setCiclo(dto.getCiclo());

        var saved = usuarioRepository.save(user);
        return toDTO(saved);
    }

    // DELETE
    @Transactional
    public void eliminarUsuario(Integer id) {
        var user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPropietarioOAdmin(user);

        usuarioRepository.delete(user); // orphanRemoval=true elimina también el Perfil
    }

    // Helpers

    private RolTipo resolveRol(String raw) {
        if (raw == null || raw.isBlank()) return RolTipo.ROLE_FREE; // default
        try {
            return RolTipo.valueOf(raw.trim().toUpperCase()); // FREE, PREMIUM, ADMIN
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("RolTipo inválido. Permitidos: FREE, PREMIUM, ADMIN.");
        }
    }
      
     //GET stats 
    @Transactional(readOnly = true)
    public UsuarioStatsResponseDTO obtenerEstadisticas(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

        long totalRecursos = recursoRepository.countByAutorId(idUsuario);
        long totalComentarios = comentarioRepository.countByAutorId(idUsuario);

        long totalResenas = resenaRepository.countResenasRecibidasPorAutor(idUsuario);
        long totalResenasPos = resenaRepository.countResenasPositivasPorAutor(idUsuario);
        long totalResenasNeg = resenaRepository.countResenasNegativasPorAutor(idUsuario);

        long totalItemsBiblioteca = bibliotecaRecursoRepository.countItemsByUsuarioId(idUsuario);

        return UsuarioStatsResponseDTO.builder()
                .totalRecursosPublicados(totalRecursos)
                .totalComentariosRealizados(totalComentarios)
                .totalResenasRecibidas(totalResenas)
                .totalResenasPositivas(totalResenasPos)
                .totalResenasNegativas(totalResenasNeg)
                .totalItemsBiblioteca(totalItemsBiblioteca)
                .build();
    }

    // GET /usuarios/me - perfil del usuario autenticado | NUEVO, PARA LOS DATOS AL FRONT
    @Transactional(readOnly = true)
    public UserResponseDTO obtenerUsuarioActualDTO() {
        Usuario auth = getAuthenticatedUsuario(); // usa el email del token
        return toDTO(auth);
    }


    private UserResponseDTO toDTO(Usuario u) {
        return new UserResponseDTO(
                u.getId_usuario(),
                u.getEmail(),
                u.getRolTipo().name(), // "FREE"/"PREMIUM"/"ADMIN"
                u.getPerfil() == null ? null :
                        new PerfilResponseDTO(
                                u.getPerfil().getId_usuario(),
                                u.getPerfil().getNombre(),
                                u.getPerfil().getApellidos(),
                                u.getPerfil().getUniversidad(),
                                u.getPerfil().getCarrera(),
                                u.getPerfil().getCiclo()
                        )
        );
    }

    // Auth
    // @Transactional(readOnly = true)
    public Usuario getAuthenticatedUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new AccessDeniedException("No autenticado");
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    // helper validar usuario propietario o admin
    private void validarPropietarioOAdmin(Usuario objetivo) {
        Usuario auth = getAuthenticatedUsuario(); // el usuario del token

        // ADMIN, puede operar sobre cualquier usuario
        if (auth.getRolTipo() == RolTipo.ROLE_ADMIN) {
            return;
        }

        // NO ADMIN, solo puede operar sobre su propio id
        if (!auth.getId_usuario().equals(objetivo.getId_usuario())) {
            throw new AccessDeniedException("No puedes operar sobre otra cuenta de usuario");
        }
    }
}
