package com.recolectaedu.service;

import com.recolectaedu.dto.request.PerfilRequestDTO;
import com.recolectaedu.dto.response.PerfilResponseDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
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


    @Transactional
    public UserResponseDTO registrarUsuario(UserRequestDTO r) {
        if (usuarioRepository.existsByEmail(r.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado");
        }

        Rol rol = (r.getRol() == null || r.getRol().isBlank())
                ? Rol.FREE
                : Rol.valueOf(r.getRol().toUpperCase());

        Usuario u = Usuario.builder()
                .email(r.getEmail())
                .password_hash(passwordEncoder.encode(r.getPassword()))
                .rol(rol)
                .build();

        // perfil opcional
        if (r.getPerfil() != null) {
            var pr = r.getPerfil();
            Perfil p = Perfil.builder()
                    .nombre(pr.getNombre())
                    .apellidos(pr.getApellidos())
                    .universidad(pr.getUniversidad())
                    .carrera(pr.getCarrera())
                    .ciclo(pr.getCiclo())
                    .build();
            u.attachPerfil(p); // mantiene ambos lados (MapsId)
        }

        Usuario saved = usuarioRepository.save(u);

        // devolver DTO (sin password)
        return new UserResponseDTO(
                saved.getId_usuario(),
                saved.getEmail(),
                saved.getRol().name(),
                saved.getPerfil() == null ? null :
                        new PerfilResponseDTO(
                                saved.getPerfil().getId_usuario(),
                                saved.getPerfil().getNombre(),
                                saved.getPerfil().getApellidos(),
                                saved.getPerfil().getUniversidad(),
                                saved.getPerfil().getCarrera(),
                                saved.getPerfil().getCiclo()
                        )
        );
    }


    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }


    // Actualiza/crea el Perfil (1:1) del usuario.
    @Transactional
    public Usuario actualizarPerfil(Integer id, PerfilRequestDTO perfilRequestDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = Perfil.builder().build();
            usuario.attachPerfil(perfil); // vincula ambos lados (MapsId)
        }

        perfil.setNombre(perfilRequestDTO.getNombre());
        perfil.setApellidos(perfilRequestDTO.getApellidos());
        perfil.setUniversidad(perfilRequestDTO.getUniversidad());
        perfil.setCarrera(perfilRequestDTO.getCarrera());
        perfil.setCiclo(perfilRequestDTO.getCiclo());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuarioRepository.delete(usuario);
    }


}
