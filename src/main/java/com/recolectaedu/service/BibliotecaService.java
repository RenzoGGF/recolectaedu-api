package com.recolectaedu.service;
import com.recolectaedu.dto.response.BibliotecaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BibliotecaService {

    private final UsuarioRepository usuarioRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public BibliotecaResponseDTO crearBiblioteca() {
        Usuario usuario = usuarioService.getAuthenticatedUsuario();

        bibliotecaRepository.findByUsuario(usuario)
                .ifPresent(biblioteca -> {
                    throw new BusinessRuleException("El usuario ya tiene una biblioteca");
                });

        Biblioteca biblioteca = Biblioteca.builder()
                .usuario(usuario)
                .nombre("Mi biblioteca")
                .build();

        Biblioteca guardado = bibliotecaRepository.save(biblioteca);

        return BibliotecaResponseDTO.builder()
                .id_biblioteca(guardado.getId_biblioteca())
                .nombre(guardado.getNombre())
                .id_usuario(guardado.getUsuario().getId_usuario())
                .build();
    }

    @Transactional
    public BibliotecaResponseDTO obtenerBIbliotecaDeUsuario() {
        Usuario usuario = usuarioService.getAuthenticatedUsuario();

        Biblioteca biblioteca = bibliotecaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        return BibliotecaResponseDTO.builder()
                .id_biblioteca(biblioteca.getId_biblioteca())
                .nombre(biblioteca.getNombre())
                .id_usuario(biblioteca.getUsuario().getId_usuario())
                .build();
    }
}