package com.recolectaedu.service;
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

    @Transactional
    public Biblioteca crearBiblioteca(Integer id_usuario) {
        Usuario usuario = usuarioRepository.findById(id_usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        bibliotecaRepository.findByUsuario(usuario)
                .ifPresent(biblioteca -> {
                    throw new BusinessRuleException("El usuario ya tiene una biblioteca");
                });

        Biblioteca biblioteca = Biblioteca.builder()
                .usuario(usuario)
                .nombre("Mi biblioteca")
                .build();

        return bibliotecaRepository.save(biblioteca);
    }

    @Transactional
    public Biblioteca obtenerBibliotecaPorUsuarioId(Integer id_usuario) {
        Usuario usuario = usuarioRepository.findById(id_usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return bibliotecaRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
    }
}