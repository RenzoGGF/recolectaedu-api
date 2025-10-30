package com.recolectaedu.service;

import com.recolectaedu.dto.request.LoginRequestDTO;
import com.recolectaedu.dto.request.UserRequestDTO;
import com.recolectaedu.dto.response.AuthResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.repository.UsuarioRepository;
import com.recolectaedu.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private RolTipo resolveRol(String raw) {
        if (raw == null || raw.isBlank()) return RolTipo.ROLE_FREE; // default
        try {
            return RolTipo.valueOf(raw.trim().toUpperCase()); // FREE, PREMIUM, ADMIN
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("RolTipo inválido. Permitidos: ROLE_FREE, ROLE_PREMIUM, ROLE_ADMIN.");
        }
    }

    @Transactional
    public AuthResponseDTO register(UserRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password_hash(passwordEncoder.encode(request.getPassword()))
                .rolTipo(resolveRol(request.getRol()))
                .build();

        if (request.getPerfil() != null) {
            var pr = request.getPerfil();
            Perfil p = Perfil.builder()
                    .nombre(pr.getNombre())
                    .apellidos(pr.getApellidos())
                    .universidad(pr.getUniversidad())
                    .carrera(pr.getCarrera())
                    .ciclo(pr.getCiclo())
                    .build();
            usuario.attachPerfil(p); // establece ambos lados (MapsId)
        }

        Usuario usuario_guardado = usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario_guardado.getEmail(), usuario_guardado.getPerfil().getNombre());

        return new AuthResponseDTO(token, usuario_guardado.getEmail(), usuario_guardado.getPerfil().getNombre());
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email()).
                orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();

        String token = jwtUtil.generateToken(usuario.getEmail(), perfil.getNombre());

        return new AuthResponseDTO(token, usuario.getEmail(), perfil.getNombre());
    }



}
