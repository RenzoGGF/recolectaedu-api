package com.recolectaedu.service;

import com.recolectaedu.dto.request.BibliotecaRecursoRequestDTO;
import com.recolectaedu.dto.response.BibliotecaRecursoResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Biblioteca;
import com.recolectaedu.model.BibliotecaRecurso;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.BibliotecaRecursoRepository;
import com.recolectaedu.repository.BibliotecaRepository;
import com.recolectaedu.repository.RecursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BibliotecaRecursoService - Pruebas unitarias")
public class BibliotecaRecursoServiceTest {
    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private BibliotecaRepository bibliotecaRepository;

    @Mock
    private BibliotecaRecursoRepository bibliotecaRecursoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private BibliotecaRecursoService bibliotecaRecursoService;

    private Usuario mockUsuario;
    private Biblioteca mockBiblioteca;
    private Recurso mockRecurso;

    @BeforeEach
    void setUp() {
        mockUsuario = createUsuarioMock(1, "john@example.com");
        mockBiblioteca = createBibliotecaMock(1, "Mi Biblioteca", mockUsuario);
        mockRecurso = createRecursoMock(1, "Recurso Test");
    }

    private Usuario createUsuarioMock(Integer id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setEmail(email);
        return usuario;
    }

    private Biblioteca createBibliotecaMock(Integer id, String nombre, Usuario usuario) {
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setId_biblioteca(id);
        biblioteca.setNombre(nombre);
        biblioteca.setUsuario(usuario);
        return biblioteca;
    }

    private Recurso createRecursoMock(Integer id, String titulo) {
        Recurso recurso = new Recurso();
        recurso.setId_recurso(id);
        recurso.setTitulo(titulo);
        return recurso;
    }

    // guardarRecursoEnBiblioteca
    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe guardar un recurso exitosamente")
    void guardarRecursoEnBiblioteca_ValidData_Success() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(bibliotecaRecursoRepository.existsByBibliotecaAndRecurso(mockBiblioteca, mockRecurso)).thenReturn(false);

        BibliotecaRecurso saved = BibliotecaRecurso.builder()
                .id_biblioteca_recurso(10)
                .biblioteca(mockBiblioteca)
                .recurso(mockRecurso)
                .agregado_el(LocalDateTime.now())
                .build();
        when(bibliotecaRecursoRepository.save(any(BibliotecaRecurso.class))).thenReturn(saved);

        BibliotecaRecursoResponseDTO response = bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request);

        assertThat(response).isNotNull();
        assertThat(response.id_biblioteca_recurso()).isEqualTo(10);
        assertThat(response.titulo_recurso()).isEqualTo("Recurso Test");
        verify(bibliotecaRecursoRepository).save(any(BibliotecaRecurso.class));
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe enviar los valores correctos a save")
    void guardarRecursoEnBiblioteca_SaveValues_Correct() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(bibliotecaRecursoRepository.existsByBibliotecaAndRecurso(mockBiblioteca, mockRecurso)).thenReturn(false);
        when(bibliotecaRecursoRepository.save(any(BibliotecaRecurso.class))).thenAnswer(inv -> {
            BibliotecaRecurso br = inv.getArgument(0);
            return BibliotecaRecurso.builder()
                    .id_biblioteca_recurso(99)
                    .biblioteca(br.getBiblioteca())
                    .recurso(br.getRecurso())
                    .agregado_el(br.getAgregado_el() != null ? br.getAgregado_el() : LocalDateTime.now())
                    .build();
        });

        BibliotecaRecursoResponseDTO response = bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request);

        ArgumentCaptor<BibliotecaRecurso> captor = ArgumentCaptor.forClass(BibliotecaRecurso.class);
        verify(bibliotecaRecursoRepository).save(captor.capture());
        BibliotecaRecurso enviado = captor.getValue();

        assertThat(enviado.getBiblioteca().getId_biblioteca()).isEqualTo(id_biblioteca);
        assertThat(enviado.getRecurso().getId_recurso()).isEqualTo(id_recurso);
        assertThat(response.id_biblioteca_recurso()).isEqualTo(99);
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe fallar si request es null")
    void guardarRecursoEnBiblioteca_NullRequest_Throws() {
        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El id del recurso es obligatorio");
        verifyNoInteractions(usuarioService, recursoRepository, bibliotecaRepository, bibliotecaRecursoRepository);
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe fallar si id_recurso es null")
    void guardarRecursoEnBiblioteca_NullRecursoId_Throws() {
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(null).build();

        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El id del recurso es obligatorio");
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe lanzar ResourceNotFound si recurso no existe")
    void guardarRecursoEnBiblioteca_ResourceNotFound_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 999;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recurso no encontrado");
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe lanzar ResourceNotFound si biblioteca no existe")
    void guardarRecursoEnBiblioteca_BibliotecaNotFound_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Biblioteca no encontrada");
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe lanzar BusinessRule si no autorizado")
    void guardarRecursoEnBiblioteca_NotAuthorized_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        Usuario otroUsuario = createUsuarioMock(2, "other@example.com");
        Biblioteca otraBiblioteca = createBibliotecaMock(id_biblioteca, "Otra", otroUsuario);

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(otraBiblioteca));

        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No autorizado para operar sobre esta biblioteca");
    }

    @Test
    @DisplayName("guardarRecursoEnBiblioteca: debe lanzar BusinessRule si recurso duplicado en biblioteca")
    void guardarRecursoEnBiblioteca_Duplicate_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;
        BibliotecaRecursoRequestDTO request = BibliotecaRecursoRequestDTO.builder().id_recurso(id_recurso).build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(bibliotecaRecursoRepository.existsByBibliotecaAndRecurso(mockBiblioteca, mockRecurso)).thenReturn(true);

        assertThatThrownBy(() -> bibliotecaRecursoService.guardarRecursoEnBiblioteca(id_biblioteca, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No pueden existir duplicados en la biblioteca");
    }

    // listarRecursos
    @Test
    @DisplayName("listarRecursos: debe listar recursos de la biblioteca del usuario autenticado")
    void listarRecursos_ValidData_Success() {
        Integer id_biblioteca = 1;

        BibliotecaRecurso br1 = BibliotecaRecurso.builder()
                .id_biblioteca_recurso(1)
                .biblioteca(mockBiblioteca)
                .recurso(createRecursoMock(11, "A"))
                .agregado_el(LocalDateTime.now())
                .build();
        BibliotecaRecurso br2 = BibliotecaRecurso.builder()
                .id_biblioteca_recurso(2)
                .biblioteca(mockBiblioteca)
                .recurso(createRecursoMock(22, "B"))
                .agregado_el(LocalDateTime.now())
                .build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(bibliotecaRecursoRepository.findByBiblioteca(mockBiblioteca)).thenReturn(List.of(br1, br2));

        List<BibliotecaRecursoResponseDTO> list = bibliotecaRecursoService.listarRecursos(id_biblioteca);

        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).titulo_recurso()).isEqualTo("A");
        assertThat(list.get(1).titulo_recurso()).isEqualTo("B");
    }

    @Test
    @DisplayName("listarRecursos: debe lanzar ResourceNotFound si biblioteca no existe")
    void listarRecursos_BibliotecaNotFound_Throws() {
        Integer id_biblioteca = 999;

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.listarRecursos(id_biblioteca))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Biblioteca no encontrada");
    }

    @Test
    @DisplayName("listarRecursos: debe lanzar BusinessRule si no autorizado")
    void listarRecursos_NotAuthorized_Throws() {
        Integer id_biblioteca = 1;

        Usuario otroUsuario = createUsuarioMock(2, "other@example.com");
        Biblioteca otraBiblioteca = createBibliotecaMock(id_biblioteca, "Otra", otroUsuario);

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(otraBiblioteca));

        assertThatThrownBy(() -> bibliotecaRecursoService.listarRecursos(id_biblioteca))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No autorizado para operar sobre esta biblioteca");
    }

    // eliminarRecurso
    @Test
    @DisplayName("eliminarRecurso: debe eliminar un recurso existente en la biblioteca")
    void eliminarRecurso_ValidData_Success() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;

        BibliotecaRecurso br = BibliotecaRecurso.builder()
                .id_biblioteca_recurso(50)
                .biblioteca(mockBiblioteca)
                .recurso(mockRecurso)
                .agregado_el(LocalDateTime.now())
                .build();

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRecursoRepository.findByBibliotecaAndRecurso(mockBiblioteca, mockRecurso)).thenReturn(Optional.of(br));

        bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso);

        verify(bibliotecaRecursoRepository).delete(br);
    }

    @Test
    @DisplayName("eliminarRecurso: debe lanzar ResourceNotFound si biblioteca no existe")
    void eliminarRecurso_BibliotecaNotFound_Throws() {
        Integer id_biblioteca = 999;
        Integer id_recurso = 1;

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Biblioteca no encontrada");
    }

    @Test
    @DisplayName("eliminarRecurso: debe lanzar BusinessRule si no autorizado")
    void eliminarRecurso_NotAuthorized_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;

        Usuario otroUsuario = createUsuarioMock(2, "other@example.com");
        Biblioteca otraBiblioteca = createBibliotecaMock(id_biblioteca, "Otra", otroUsuario);

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(otraBiblioteca));

        assertThatThrownBy(() -> bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No autorizado para operar sobre esta biblioteca");
    }

    @Test
    @DisplayName("eliminarRecurso: debe lanzar ResourceNotFound si recurso no existe")
    void eliminarRecurso_RecursoNotFound_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 999;

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recurso no encontrado");
    }

    @Test
    @DisplayName("eliminarRecurso: debe lanzar ResourceNotFound si el recurso no está en la biblioteca")
    void eliminarRecurso_NotInBiblioteca_Throws() {
        Integer id_biblioteca = 1;
        Integer id_recurso = 1;

        when(usuarioService.getAuthenticatedUsuario()).thenReturn(mockUsuario);
        when(bibliotecaRepository.findById(id_biblioteca)).thenReturn(Optional.of(mockBiblioteca));
        when(recursoRepository.findById(id_recurso)).thenReturn(Optional.of(mockRecurso));
        when(bibliotecaRecursoRepository.findByBibliotecaAndRecurso(mockBiblioteca, mockRecurso)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bibliotecaRecursoService.eliminarRecurso(id_biblioteca, id_recurso))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recurso no encontrado en la biblioteca");
    }
}