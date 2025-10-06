package com.recolectaedu.service;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.PerfilResponseDTO;
import com.recolectaedu.dto.response.UserResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.Rol;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // POST
    @Transactional
    public UserResponseDTO registrarUsuario(UserRequestDTO r) {
        if (usuarioRepository.existsByEmail(r.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado");
        }

        Usuario u = Usuario.builder()
                .email(r.getEmail())
                .password_hash(passwordEncoder.encode(r.getPassword()))
                .rol(resolveRol(r.getRol()))
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
        return toDTO(user);
    }

    // PUT
    @Transactional
    public UserResponseDTO actualizarPerfilDTO(Integer id, PerfilRequestDTO dto) {
        var user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

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
        usuarioRepository.delete(user); // orphanRemoval=true elimina también el Perfil
    }

    // Helpers

    private Rol resolveRol(String raw) {
        if (raw == null || raw.isBlank()) return Rol.FREE; // default
        try {
            return Rol.valueOf(raw.trim().toUpperCase()); // FREE, PREMIUM, ADMIN
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Rol inválido. Permitidos: FREE, PREMIUM, ADMIN.");
        }
    }

    private UserResponseDTO toDTO(Usuario u) {
        return new UserResponseDTO(
                u.getId_usuario(),
                u.getEmail(),
                u.getRol().name(), // "FREE"/"PREMIUM"/"ADMIN"
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
}
