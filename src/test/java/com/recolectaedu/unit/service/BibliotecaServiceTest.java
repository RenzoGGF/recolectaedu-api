package com.recolectaedu.unit.service;

import com.recolectaedu.dto.response.BibliotecaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.service.BibliotecaService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private BibliotecaRepository bibliotecaRepository;

    @InjectMocks
    private BibliotecaService bibliotecaService;

    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        mockUsuario = createUsuarioMock(1, "john@example.com");
    }

    @Test
    @DisplayName("Crear biblioteca: debe crear biblioteca correctamente")
    void crearBiblioteca_Success() {
        // Arrange
        setupAuthentication();
        when(bibliotecaRepository.findByUsuario(mockUsuario)).thenReturn(Optional.empty());

        Biblioteca savedBiblioteca = createBibliotecaMock(1, mockUsuario);
        when(bibliotecaRepository.save(any(Biblioteca.class))).thenReturn(savedBiblioteca);

        // Act
        BibliotecaResponseDTO result = bibliotecaService.crearBiblioteca();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.id_biblioteca());
        assertEquals("Mi biblioteca", result.nombre());
        assertEquals(1, result.id_usuario());

        verify(usuarioService).getAuthenticatedUsuario();
        verify(bibliotecaRepository).findByUsuario(mockUsuario);
        verify(bibliotecaRepository).save(any(Biblioteca.class));
    }

    @Test
    @DisplayName("Debe lanzar una excepción por duplicidad")
    void crearBiblioteca_Duplicated_ThrowsException() {
        // Arrange
        setupAuthentication();
        Biblioteca existingBiblioteca = createBibliotecaMock(1, mockUsuario);
        when(bibliotecaRepository.findByUsuario(mockUsuario)).thenReturn(Optional.of(existingBiblioteca));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> bibliotecaService.crearBiblioteca());

        assertEquals("El usuario ya tiene una biblioteca", exception.getMessage());
        verify(usuarioService).getAuthenticatedUsuario();
        verify(bibliotecaRepository).findByUsuario(mockUsuario);
        verify(bibliotecaRepository, never()).save(any(Biblioteca.class));
    }

    @Test
    @DisplayName("Debe obtener la biblioteca del usuario")
    void obtenerBibliotecaDeUsuario_Success() {
        // Arrange
        setupAuthentication();
        Biblioteca biblioteca = createBibliotecaMock(1, mockUsuario);
        when(bibliotecaRepository.findByUsuario(mockUsuario)).thenReturn(Optional.of(biblioteca));

        // Act
        BibliotecaResponseDTO result = bibliotecaService.obtenerBIbliotecaDeUsuario();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.id_biblioteca());
        assertEquals("Mi biblioteca", result.nombre());
        assertEquals(1, result.id_usuario());

        verify(usuarioService).getAuthenticatedUsuario();
        verify(bibliotecaRepository).findByUsuario(mockUsuario);
    }

    @Test
    @DisplayName("Debe lanzar una excepción por no existir la biblioteca")
    void obtenerBibliotecaDeUsuario_NotFound_ThrowsException() {
        // Arrange
        setupAuthentication();
        when(bibliotecaRepository.findByUsuario(mockUsuario)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bibliotecaService.obtenerBIbliotecaDeUsuario());

        assertEquals("Biblioteca no encontrada", exception.getMessage());
        verify(usuarioService).getAuthenticatedUsuario();
        verify(bibliotecaRepository).findByUsuario(mockUsuario);
    }

    private Biblioteca createBibliotecaMock(Integer id_biblioteca, Usuario usuario) {
        return Biblioteca.builder()
                .id_biblioteca(1)
                .usuario(usuario)
                .nombre("Mi biblioteca")
                .build();
    }

    private Usuario createUsuarioMock(Integer id, String email) {
        return Usuario.builder()
                .id_usuario(id)
                .email(email)
                .build();
    }

    private void setupAuthentication() {
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
    }
}