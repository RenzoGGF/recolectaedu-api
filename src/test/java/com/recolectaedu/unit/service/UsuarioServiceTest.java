package com.recolectaedu.unit.service;

import com.recolectaedu.dto.response.UsuarioStatsResponseDTO;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.*;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private BibliotecaRecursoRepository bibliotecaRecursoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new Usuario();
        mockUser.setId_usuario(1);
    }

    @Test
    @DisplayName("Debe mostrar 5 en contador de recursos publicados cuando el usuario tiene 5 recursos")
    void obtenerEstadisticas_usuarioCon5Recursos_retorna5EnContador() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(5L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalRecursosPublicados()).isEqualTo(5);
        verify(recursoRepository).countByAutorId(1);
    }

    @Test
    @DisplayName("Debe mostrar 12 en contador de comentarios cuando el usuario ha realizado 12 comentarios")
    void obtenerEstadisticas_usuarioCon12Comentarios_retorna12EnContador() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(12L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalComentariosRealizados()).isEqualTo(12);
        verify(comentarioRepository).countByAutorId(1);
    }

    @Test
    @DisplayName("Debe mostrar 8 votos positivos y 2 negativos cuando los recursos han recibido esos votos")
    void obtenerEstadisticas_recursosCon8VotosPositivosY2Negativos_retornaContadoresCorrectos() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(10L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(8L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(2L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalResenasPositivas()).isEqualTo(8);
        assertThat(response.totalResenasNegativas()).isEqualTo(2);
        verify(resenaRepository).countResenasPositivasPorAutor(1);
        verify(resenaRepository).countResenasNegativasPorAutor(1);
    }

    @Test
    @DisplayName("Debe mostrar 15 en contador de biblioteca personal cuando el usuario tiene 15 recursos guardados")
    void obtenerEstadisticas_usuarioCon15RecursosGuardados_retorna15EnContadorBiblioteca() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(15L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalItemsBiblioteca()).isEqualTo(15);
        verify(bibliotecaRecursoRepository).countItemsByUsuarioId(1);
    }

    @Test
    @DisplayName("Debe mostrar 0 en todos los contadores cuando el usuario no tiene actividad")
    void obtenerEstadisticas_nuevoUsuarioSinActividad_retornaTodosLosContadoresEnCero() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(recursoRepository.countByAutorId(1)).thenReturn(0L);
        when(comentarioRepository.countByAutorId(1)).thenReturn(0L);
        when(resenaRepository.countResenasRecibidasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasPositivasPorAutor(1)).thenReturn(0L);
        when(resenaRepository.countResenasNegativasPorAutor(1)).thenReturn(0L);
        when(bibliotecaRecursoRepository.countItemsByUsuarioId(1)).thenReturn(0L);

        // Act
        UsuarioStatsResponseDTO response = usuarioService.obtenerEstadisticas(1);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalRecursosPublicados()).isZero();
        assertThat(response.totalComentariosRealizados()).isZero();
        assertThat(response.totalResenasRecibidas()).isZero();
        assertThat(response.totalResenasPositivas()).isZero();
        assertThat(response.totalResenasNegativas()).isZero();
        assertThat(response.totalItemsBiblioteca()).isZero();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void obtenerEstadisticas_usuarioNoExistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtenerEstadisticas(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado: 999");
    }
}
