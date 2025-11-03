package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.ResenaRequestCreateDTO;
import com.recolectaedu.dto.request.ResenaRequestPartialUpdateDTO;
import com.recolectaedu.dto.request.ResenaRequestUpdateDTO;
import com.recolectaedu.dto.response.ResenaResponseDTO;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Resena;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.RolTipo;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import com.recolectaedu.service.ResenaService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaService - Pruebas unitarias")
public class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ResenaService resenaService;

    private Usuario mockUsuario;
    private Recurso mockRecurso;
    private Perfil mockPerfil;

    @BeforeEach
    void setUp() {
        mockUsuario = createUsuarioMock(1, "email@example.com");
        mockRecurso = createRecursoMock(1, mockUsuario, "Recurso Test");
        mockPerfil = createPerfilMock(1, mockUsuario, "Nombre", "Apellido", "Universidad", "Carrera", (short)1);
        mockUsuario.setPerfil(mockPerfil);
    }

    private Usuario createUsuarioMock(Integer id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setEmail(email);
        usuario.setRolTipo(RolTipo.ROLE_FREE);
        return usuario;
    }

    private Recurso createRecursoMock(Integer id, Usuario usuario, String nombre) {
        Recurso recurso = new Recurso();
        recurso.setId_recurso(id);
        recurso.setUsuario(usuario);
        recurso.setTitulo(nombre);
        return recurso;
    }

    private Resena createResenaMock(Integer id, String contenido, Boolean es_positivo, Usuario usuario, Recurso recurso) {
        Resena resena = new Resena();
        resena.setId_resena(id);
        resena.setContenido(contenido);
        resena.setEs_positivo(es_positivo);
        resena.setUsuario(usuario);
        resena.setRecurso(recurso);
        return resena;
    }

    private Perfil createPerfilMock(Integer id, Usuario usuario, String nombre, String apellidos, String universidad, String carrera, Short ciclo) {
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        perfil.setNombre(nombre);
        perfil.setApellidos(apellidos);
        perfil.setUniversidad(universidad);
        perfil.setCarrera(carrera);
        perfil.setCiclo(ciclo);
        return perfil;
    }

    private void setUpAuthenication(String email, Usuario usuario) {
        SecurityContextHolder.setContext(securityContext);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getName()).thenReturn(email);
//        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        // ?
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(usuario);
    }

    @Test
    @DisplayName("Reseña: debe crear reseña correctamente")
    void create_ValidData_Success() {
        Integer recurso_id = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;

        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(recurso_id, comentario, es_positivo);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);
        when(recursoRepository.findById(recurso_id)).thenReturn(Optional.of(mockRecurso));
        when(resenaRepository.existsByUsuarioAndRecurso(mockUsuario, mockRecurso)).thenReturn(false);

        Resena resenaGuardada = createResenaMock(1, comentario, es_positivo, mockUsuario, mockRecurso);

        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaGuardada);

        ResenaResponseDTO response = resenaService.crearResena(request);

        assertThat(response).isNotNull();
        assertThat(response.id_resena()).isEqualTo(1);
        assertThat(response.es_positivo()).isTrue();
        assertThat(response.contenido()).isEqualTo(comentario);

        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("Reseña: debe lanzar excepción al intentar crear reseña dupulicada")
    void create_DuplicateData_ThrowsException() {
        Integer recurso_id = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;

        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(recurso_id, comentario, es_positivo);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        when(recursoRepository.findById(recurso_id)).thenReturn(Optional.of(mockRecurso));
        when(resenaRepository.existsByUsuarioAndRecurso(mockUsuario, mockRecurso)).thenReturn(true);

        assertThatThrownBy(() -> resenaService.crearResena(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El usuario ya ha dado una reseña al recurso");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar una exepción al intentar reseñar un recurso que no exista")
    void create_ResourceNotFound_ThrowsException() {
        Integer recurso_id = 999;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;

        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(recurso_id, comentario, es_positivo);

        when(recursoRepository.findById(recurso_id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resenaService.crearResena(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recurso no encontrado");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el comentario está vacío")
    void createResena_ComentarioNulo_ThrowsException() {
        Integer id_recurso = 1;
        String comentario = null;
        Boolean es_positivo = true;
        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(id_recurso, comentario, es_positivo);

        assertThatThrownBy(() -> resenaService.crearResena(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El comentario no puede estar vacío");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando es_positivo es nulo")
    void createResena_EsPositivoNulo_ThrowsException() {
        Integer id_recurso = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = null;
        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(id_recurso, comentario, es_positivo);

        assertThatThrownBy(() -> resenaService.crearResena(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El valor del voto no puede estar vacío");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe eliminar la reseña correctamente, verificando la autoría")
    void delete_ValidOwnership_Success() {
        Integer resenaId = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;
        Resena mockResena = createResenaMock(resenaId, comentario, es_positivo, mockUsuario, mockRecurso);
        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(mockResena)); // Simulamos que la reseña existe
        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        resenaService.eliminar(resenaId);

        verify(resenaRepository, times(1)).delete(mockResena); // Verificamos que se haya llamado al delete
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no es el propietario de la reseña")
    void delete_InvalidOwnership_ThrowsException() {
        Integer resenaId = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;
        Resena mockResena = createResenaMock(resenaId, comentario, es_positivo, mockUsuario, mockRecurso);
        Usuario mockOtroUsuario = new Usuario();
        mockOtroUsuario.setId_usuario(2);

        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(mockResena));
        setUpAuthenication(mockOtroUsuario.getEmail(), mockOtroUsuario);

        assertThatThrownBy(() -> resenaService.eliminar(resenaId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso para operar sobre esta reseña");

        verify(resenaRepository, never()).delete(any(Resena.class));
    }

    @Test
    @DisplayName("Debe actualizar completamente la reseña")
    void update_ValidData_Success() {
        Integer resenaId = 1;
        String nuevoContenido = "Nuevo comentario";
        Boolean nuevoVoto = false;

        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, nuevoVoto);
        Resena resenaExistente = createResenaMock(resenaId, "Comentario original", true, mockUsuario, mockRecurso);

        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(resenaExistente));
        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        Resena resenaActualizada = new Resena();
        resenaActualizada.setId_resena(resenaId);
        resenaActualizada.setContenido(nuevoContenido);
        resenaActualizada.setEs_positivo(nuevoVoto);
        resenaActualizada.setUsuario(mockUsuario);
        resenaActualizada.setRecurso(mockRecurso);
        resenaActualizada.setCreado_el(resenaExistente.getCreado_el());
        resenaActualizada.setActualizado_el(LocalDateTime.now());

        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaActualizada);

        ResenaResponseDTO response = resenaService.actualizarResena(resenaId, request);

        assertThat(response).isNotNull();
        assertThat(response.contenido()).isEqualTo(nuevoContenido);
        assertThat(response.es_positivo()).isEqualTo(nuevoVoto);
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no es el propietario de la reseña")
    void update_InvalidOwnership_ThrowsException() {
        Integer resenaId = 1;
        String nuevoContenido = "Nuevo comentario";
        Boolean nuevoVoto = false;

        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, nuevoVoto);
        Resena resenaExistente = createResenaMock(resenaId, "Comentario original", true, mockUsuario, mockRecurso);

        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(resenaExistente));
        Usuario otroUsuario = new Usuario();
        setUpAuthenication(otroUsuario.getEmail(), otroUsuario);

        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso para operar sobre esta reseña");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe actualizar parcialmente la reseña (solo el voto)")
    void partialUpdate_Voto_Success() {
        Integer resenaId = 1;
        Boolean nuevoVoto = false;

        ResenaRequestPartialUpdateDTO request = new ResenaRequestPartialUpdateDTO(null, nuevoVoto);
        Resena resenaExistente = createResenaMock(resenaId, "Comentario original", true, mockUsuario, mockRecurso);

        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(resenaExistente));
        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        resenaExistente.setEs_positivo(nuevoVoto);
        resenaExistente.setActualizado_el(LocalDateTime.now());

        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaExistente);

        ResenaResponseDTO response = resenaService.actualizarParcialResena(resenaId, request);

        assertThat(response).isNotNull();
        assertThat(response.es_positivo()).isEqualTo(nuevoVoto);
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no es el propietario de la reseña al actualizar parcialmente")
    void partialUpdate_InvalidOwnership_ThrowsException() {
        Integer resenaId = 1;
        Boolean nuevoVoto = false;
        ResenaRequestPartialUpdateDTO request = new ResenaRequestPartialUpdateDTO(null, nuevoVoto);
        Resena resenaExistente = createResenaMock(resenaId, "Comentario original", true, mockUsuario, mockRecurso);

        when(resenaRepository.findById(resenaId)).thenReturn(Optional.of(resenaExistente));
        Usuario otroUsuario = new Usuario();
        setUpAuthenication(otroUsuario.getEmail(), otroUsuario);

        assertThatThrownBy(() -> resenaService.actualizarParcialResena(resenaId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso para operar sobre esta reseña");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato de 'es_positivo' no es un booleano")
    void update_InvalidVotoFormat_ThrowsException() {
        Integer resenaId = 1;
        String nuevoContenido = "Nuevo comentario";
        String formatoIncorrectoVoto = "No es booleano";

        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, (Boolean) null);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El valor del voto no puede estar vacío");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el contenido de la reseña está vacío")
    void update_EmptyContent_ThrowsException() {
        Integer resenaId = 1;
        String contenidoVacio = "";
        Boolean nuevoVoto = true;

        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(contenidoVacio, nuevoVoto);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El comentario no puede estar vacío");

        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el voto es nulo")
    void update_NullVoto_ThrowsException() {
        Integer resenaId = 1;
        String nuevoContenido = "Nuevo comentario";
        Boolean votoNulo = null;

        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, votoNulo);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);

        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El valor del voto no puede estar vacío");

        verify(resenaRepository, never()).save(any(Resena.class));
    }
}
