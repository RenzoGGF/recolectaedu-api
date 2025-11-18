package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.model.Foro;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.service.ForoService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de Foro Service")
public class ForoServiceTest {

    @Mock
    private ForoRepository foroRepository;
    @Mock
    private UsuarioService usuarioService; //

    @InjectMocks
    private ForoService foroService;

    private Usuario usuarioMock;
    private Foro foroGuardadoMock;
    private ForoRequestDTO requestDTO;
    private LocalDateTime tiempoPrueba;

    private void setUpAuthentication(Usuario usuario) {
        given(usuarioService.getAuthenticatedUsuario()).willReturn(usuario);
    }

    @BeforeEach
    void setUp() {
        tiempoPrueba = LocalDateTime.now();
        Perfil perfilMock = Perfil.builder()
                .nombre("Nombre de prueba")
                .apellidos("Apellido de prueba")
                .build();

        usuarioMock = Usuario.builder()
                .id_usuario(1)
                .email("test@usuario.com")
                .password_hash("hash123")
                .perfil(perfilMock)           // <--- importante
                .build();

        requestDTO = new ForoRequestDTO(
                "Título del tema de prueba",
                "Este es el contenido del tema de prueba que es lo suficientemente largo."
        );

        foroGuardadoMock = Foro.builder()
                .id_foro(100)
                .titulo("Título del tema de prueba")
                .contenido("Este es el contenido del tema de prueba, es lo suficientemente largo.")
                .usuario(usuarioMock)
                .creado_el(tiempoPrueba)
                .build();
    }

    /*
    Escenario 1: Creación de tema exitosa
    DADO que estoy autenticado
    CUANDO completo los datos requeridos (titulo, descripcion)
    ENTONCES el sistema crea el tema y muestra un mensaje de confirmación.

    ID: CP-2001
    Historia: US-20
    Escenario: Creación de tema de foro exitosa
    Precondiciones:
    - El 'UsuarioService' devuelve un 'usuarioMock' (ID 1).
    - El 'ForoRequestDTO' trae un título válido y contenido válido.
    Datos de prueba:
    - requestDTO
    - usuarioMock
    - foroGuardadoMock
    Pasos:
    1. Simular `usuarioService.getAuthenticatedUsuario()` para que devuelva `usuarioMock`.
    2. Simular `foroRepository.save(any(Foro.class))` para que devuelva `foroGuardadoMock`.
    3. Ejecutar `foroService.crearTema(requestDTO)`.
    Resultado esperado:
    - Un `ForoResponseDTO` con los datos del `foroGuardadoMock`.
    - { id_foro: 100, titulo: "Título...", creado_el: ..., id_usuario: 1 }
    Explicación del test;
    GIVEN: Configuramos el mock de `usuarioService` para que devuelva un
           usuario autenticado (ID 1) y el `foroRepository` para que guarde el foro.
    WHEN:  Ejecutamos el metodo 'crearTema' con el DTO válido (sin ID).
    THEN:  Verificamos que la respuesta no es nula,
           que contiene los datos esperados (ID 100, ID 1) y
           que 'getAuthenticatedUsuario' y 'save' fueron llamados 1 vez cada uno.
    */
    @Test
    @DisplayName("E - Debe crear un tema de foro exitosamente")
    void crearTema_whenDatosValidos_shouldCrearForoExitosamente() {
        // GIVEN
        setUpAuthentication(usuarioMock); //
        given(foroRepository.save(any(Foro.class))).willReturn(foroGuardadoMock);

        // WHEN
        ForoResponseDTO response = foroService.crearTema(requestDTO);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id_foro()).isEqualTo(100);
        assertThat(response.id_usuario()).isEqualTo(1);
        assertThat(response.titulo()).isEqualTo("Título del tema de prueba");
        assertThat(response.creado_el()).isEqualTo(tiempoPrueba);

        then(usuarioService).should(times(1)).getAuthenticatedUsuario(); //
        then(foroRepository).should(times(1)).save(any(Foro.class));
    }


    /*
    Escenario 2: Creación falla por Usuario no autenticado
    DADO que el 'UsuarioService' no encuentra un usuario autenticado
    CUANDO se intenta crear un tema
    ENTONCES el sistema lanza una excepción AccessDeniedException
    Y no se crea ningún tema.

    ID: CP-2002
    Historia: US-20
    Escenario: Creación de tema de foro falla por Usuario no autenticado
    Precondiciones:
    - El 'UsuarioService' lanza una excepción al buscar al usuario.
    Datos de prueba:
    - requestDTO: { titulo: "Título...", contenido: "Contenido..." }
    Pasos:
    1. Simular `usuarioService.getAuthenticatedUsuario()` para que lance `AccessDeniedException`.
    2. Ejecutar `foroService.crearTema(requestDTO)`.
    3. Verificar que se lanza la excepción `AccessDeniedException`.
    Resultado esperado:
    - Se lanza una excepción `AccessDeniedException`.
    - El 'foroRepository.save()' no se ejecuta.

    Explicación del test;
    GIVEN: Configuramos el mock 'usuarioService' para que falle
           al intentar obtener el usuario autenticado.
    WHEN:  Ejecutamos el metodo 'crearTema'.
    THEN:  Verificamos que el sistema lanza una 'AccessDeniedException' y
           que el metodo 'foroRepository.save()' NUNCA fue invocado.
    */
    @Test
    @DisplayName("F - Debe lanzar AccessDeniedException si el usuario no está autenticado")
    void crearTema_whenUsuarioNoAutenticado_shouldThrowException() {
        // GIVEN
        ForoRequestDTO requestFalla = new ForoRequestDTO("Título válido", "Contenido válido");

        given(usuarioService.getAuthenticatedUsuario()).willThrow(new AccessDeniedException("Acceso denegado"));

        // WHEN
        assertThrows(AccessDeniedException.class, () -> {
            foroService.crearTema(requestFalla);
        });

        // THEN
        then(usuarioService).should(times(1)).getAuthenticatedUsuario();
        then(foroRepository).should(never()).save(any(Foro.class));
    }
}