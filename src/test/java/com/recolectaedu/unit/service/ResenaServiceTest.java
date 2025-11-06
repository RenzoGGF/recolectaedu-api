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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(usuario);
    }

    /*
    US-13: Reseñar un recurso
     */
    @Test
    @DisplayName("Reseña: debe crear reseña correctamente")
    void create_ValidData_Success() {
        Integer recurso_id = 1;
        String comentario = "Comentario de prueba";
        Boolean es_positivo = true;

        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(recurso_id, comentario, es_positivo);

        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);
        given(recursoRepository.findById(recurso_id)).willReturn(Optional.of(mockRecurso));
//        when(recursoRepository.findById(recurso_id)).thenReturn(Optional.of(mockRecurso));
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
    @DisplayName("crearResena: falla si no autenticado")
    void crearResena_Unauthenticated_Throws() {
        ResenaRequestCreateDTO req = new ResenaRequestCreateDTO(1, "Comentario", true);

        when(usuarioService.getAuthenticatedUsuario())
                .thenThrow(new AccessDeniedException("No autenticado"));

        assertThatThrownBy(() -> resenaService.crearResena(req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No autenticado");

        verifyNoInteractions(recursoRepository, resenaRepository);
    }

    // No se permiten varias reseñas al mismo recurso
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

    // No se puede crear reseñar a un recurso que no exista
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

    // El comentario de la reseña no puede ser nulo
//    @Test
//    @DisplayName("Debe lanzar excepción cuando el comentario está vacío")
//    void createResena_ComentarioNulo_ThrowsException() {
//        Integer id_recurso = 1;
//        String comentario = null;
//        Boolean es_positivo = true;
//        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(id_recurso, comentario, es_positivo);
//
//        assertThatThrownBy(() -> resenaService.crearResena(request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("El comentario no puede estar vacío");
//
//        verify(resenaRepository, never()).save(any(Resena.class));
//    }

    // El voto no puede ser nulo
//    @Test
//    @DisplayName("Debe lanzar excepción cuando es_positivo es nulo")
//    void createResena_EsPositivoNulo_ThrowsException() {
//        Integer id_recurso = 1;
//        String comentario = "Comentario de prueba";
//        Boolean es_positivo = null;
//        ResenaRequestCreateDTO request = new ResenaRequestCreateDTO(id_recurso, comentario, es_positivo);
//
//        assertThatThrownBy(() -> resenaService.crearResena(request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("El valor del voto no puede estar vacío");
//
//        verify(resenaRepository, never()).save(any(Resena.class));
//    }

    /*
    US-14: Votar utilidad de recursos
     */
    // Se actualiza parcialmente la reseña (solo el voto)
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

    // Solo se puede modificar la reseña de la que se es propietario
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
    @DisplayName("actualizarResena: falla si no autenticado")
    void actualizarResena_Unauthenticated_Throws() {
        var req = new ResenaRequestUpdateDTO("Nuevo", true);

        when(usuarioService.getAuthenticatedUsuario())
                .thenThrow(new IllegalStateException("No autenticado"));

        assertThatThrownBy(() -> resenaService.actualizarResena(1, req))
                .isInstanceOf(IllegalStateException.class);

        verify(resenaRepository, never()).findById(any());
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarParcialResena: falla si no autenticado")
    void actualizarParcialResena_Unauthenticated_Throws() {
        var req = new ResenaRequestPartialUpdateDTO("Nuevo parcial", null);

        when(usuarioService.getAuthenticatedUsuario())
                .thenThrow(new IllegalStateException("No autenticado"));

        assertThatThrownBy(() -> resenaService.actualizarParcialResena(1, req))
                .isInstanceOf(IllegalStateException.class);

        verify(resenaRepository, never()).findById(any());
        verify(resenaRepository, never()).save(any());
    }

    // Error si el voto no es booleano
//    @Test
//    @DisplayName("Debe lanzar excepción si el formato de 'es_positivo' no es un booleano")
//    void update_InvalidVotoFormat_ThrowsException() {
//        Integer resenaId = 1;
//        String nuevoContenido = "Nuevo comentario";
//        String formatoIncorrectoVoto = "No es booleano";
//
//        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, (Boolean) null);
//
//        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);
//
//        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("El valor del voto no puede estar vacío");
//
//        verify(resenaRepository, never()).save(any(Resena.class));
//    }

    // Error si el voto es nulo
//    @Test
//    @DisplayName("Debe lanzar excepción si el voto es nulo")
//    void update_NullVoto_ThrowsException() {
//        Integer resenaId = 1;
//        String nuevoContenido = "Nuevo comentario";
//        Boolean votoNulo = null;
//
//        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(nuevoContenido, votoNulo);
//
//        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);
//
//        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("El valor del voto no puede estar vacío");
//
//        verify(resenaRepository, never()).save(any(Resena.class));
//    }

    /*
    No pertenecen a ninguna US (o podrían ser otra US)
     */
    //
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

    // No se puede eliminar un recurso del que no se sea propietario
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

    // Actualizar una reseña por completo
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

//    @Test
//    @DisplayName("Debe lanzar excepción si el contenido de la reseña está vacío")
//    void update_EmptyContent_ThrowsException() {
//        Integer resenaId = 1;
//        String contenidoVacio = "";
//        Boolean nuevoVoto = true;
//
//        ResenaRequestUpdateDTO request = new ResenaRequestUpdateDTO(contenidoVacio, nuevoVoto);
//
//        setUpAuthenication(mockUsuario.getEmail(), mockUsuario);
//
//        assertThatThrownBy(() -> resenaService.actualizarResena(resenaId, request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("El comentario no puede estar vacío");
//
//        verify(resenaRepository, never()).save(any(Resena.class));
//    }

    /*
    Para obtener reseñas por recurso
     */
    @Test
    @DisplayName("Debe listar reseñas por recurso correctamente")
    void listByResource_ValidResource_Success() {
        Integer recursoId = 1;

        Usuario userA = createUsuarioMock(1, "a@example.com");
        Usuario userB = createUsuarioMock(2, "b@example.com");

        Perfil perfilA = createPerfilMock(1, userA, "NombreA", "ApellidoB", "Universidad", "Carrera", (short)1);
        Perfil perfilB = createPerfilMock(2, userB, "NombreB", "ApellidoB", "Universidad", "Carrera", (short)1);

        userA.setPerfil(perfilA);
        userB.setPerfil(perfilB);

        Resena resena1 = createResenaMock(1, "Comentario 1", true,  userA, mockRecurso);
        Resena resena2 = createResenaMock(2, "Comentario 2", false, userB, mockRecurso);

        given(recursoRepository.findById(recursoId)).willReturn(Optional.of(mockRecurso));
        given(resenaRepository.findByRecurso(mockRecurso)).willReturn(List.of(resena1, resena2));

        List<ResenaResponseDTO> responseList = resenaService.listarPorRecurso(recursoId);

        assertThat(responseList).isNotNull();
        assertThat(responseList).hasSize(2);

        assertThat(responseList.get(0).id_resena()).isEqualTo(1);
        assertThat(responseList.get(0).contenido()).isEqualTo("Comentario 1");
        assertThat(responseList.get(0).es_positivo()).isTrue();

        assertThat(responseList.get(1).id_resena()).isEqualTo(2);
        assertThat(responseList.get(1).contenido()).isEqualTo("Comentario 2");
        assertThat(responseList.get(1).es_positivo()).isFalse();

        verify(recursoRepository, times(1)).findById(recursoId);
        verify(resenaRepository, times(1)).findByRecurso(mockRecurso);
    }


    @Test
    @DisplayName("eliminar: falla si no autenticado")
    void eliminar_Unauthenticated_Throws() {
        when(usuarioService.getAuthenticatedUsuario())
                .thenThrow(new IllegalStateException("No autenticado"));

        assertThatThrownBy(() -> resenaService.eliminar(1))
                .isInstanceOf(IllegalStateException.class);

        verify(resenaRepository, never()).findById(any());
        verify(resenaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al listar reseñas de un recurso no existente")
    void listByResource_ResourceNotFound_ThrowsException() {
        Integer recursoId = 999;
        given(recursoRepository.findById(recursoId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> resenaService.listarPorRecurso(recursoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recurso no encontrado");

        verify(recursoRepository, times(1)).findById(recursoId);
        verify(resenaRepository, never()).findByRecurso(any());
    }
}
