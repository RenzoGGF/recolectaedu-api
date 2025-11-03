package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Foro;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.ForoRepository;
import com.recolectaedu.repository.UsuarioRepository;
import com.recolectaedu.service.ForoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;


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

    //PONER DEPENDENCIAS DE DIAGRAMAS DE COMPONENTES
    @Mock
    private ForoRepository foroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ForoService foroService;

    private Usuario usuarioMock;
    private Foro foroGuardadoMock;
    private ForoRequestDTO requestDTO;
    private LocalDateTime tiempoPrueba;

    @BeforeEach
    void setUp() {
        tiempoPrueba = LocalDateTime.now();
        usuarioMock = Usuario.builder()
                .id_usuario(1)
                .email("test@usuario.com")
                .password_hash("hash123")
                .build();

        requestDTO = new ForoRequestDTO(
                "Título del tema de prueba",
                "Este es el contenido del tema de prueba que es lo suficientemente largo.",
                1
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
    DADO que estoy autenticado y me encuentro en el formulario de creación del tema del foro
    CUANDO completo los datos requeridos (titulo, descripcion) y presiono en “Crear”
    ENTONCES el sistema crea el tema y muestra un mensaje de confirmación.

    ID: CP-2001
    Historia: US-20
    Escenario: Creación de tema de foro exitosa
    Precondiciones:
    - Un 'Usuario' existe en la BD con ID 1.
    - El 'ForoRequestDTO' trae un título válido, contenido válido y el ID 1 del usuario.
    Datos de prueba:
    - requestDTO: { titulo: "Título...", contenido: "Contenido...", id_usuario: 1 }
    - usuarioMock: { id: 1, email: "..." }
    - foroGuardadoMock: { id: 100, titulo: "...", usuario: usuarioMock, creado_el: ... }
    Pasos:
    1. Simular `usuarioRepository.findById(1)` para que devuelva `Optional.of(usuarioMock)`.
    2. Simular `foroRepository.save(any(Foro.class))` para que devuelva `foroGuardadoMock`.
    3. Ejecutar `foroService.crearTema(requestDTO)`.
    Resultado esperado:
    - Un `ForoResponseDTO` con los datos del `foroGuardadoMock`.
    - { id_foro: 100, titulo: "Título...", creado_el: ..., id_usuario: 1 }

    Explicación del test;
    GIVEN: Configuramos mocks para un 'Usuario' (ID 1) que sí existe y
           configuramos el 'foroRepository' para que devuelva el objeto 'Foro'
           simulado cuando se llame a save().
    WHEN:  Ejecutamos el metodo 'crearTema' con el DTO válido.
    THEN:  Verificamos que la respuesta no es nula,
           que contiene los datos esperados (ID 100, ID 1) y
           que 'findById' y 'save' fueron llamados 1 vez cada uno.
    */

    @Test
    @DisplayName("Debe crear un tema de foro exitosamente")
    void crearTema_whenDatosValidos_shouldCrearForoExitosamente() {
        // GIVEN
        given(usuarioRepository.findById(1)).willReturn(Optional.of(usuarioMock));
        given(foroRepository.save(any(Foro.class))).willReturn(foroGuardadoMock);

        // WHEN
        ForoResponseDTO response = foroService.crearTema(requestDTO);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id_foro()).isEqualTo(100);
        assertThat(response.id_usuario()).isEqualTo(1);
        assertThat(response.titulo()).isEqualTo("Título del tema de prueba");
        assertThat(response.creado_el()).isEqualTo(tiempoPrueba);
        then(usuarioRepository).should(times(1)).findById(1);
        then(foroRepository).should(times(1)).save(any(Foro.class));
    }


    /*
    Escenario 2: Creación falla (Usuario no existe)
    DADO que los datos de validación (título, contenido) son correctos
    CUANDO el 'id_usuario' enviado no corresponde a un usuario existente
    ENTONCES el sistema no crea un nuevo tema y lanza un mensaje de error.

    ID: CP-2002
    Historia: US-20
    Escenario: Creación de tema de foro falla (Usuario no existe)
    Precondiciones:
    - El 'ForoRequestDTO' tiene un 'id_usuario' (ej. 99) que NO existe.
    Datos de prueba:
    - requestFalla: { titulo: "Título válido", contenido: "Contenido válido", id_usuario: 99 }
    Pasos:
    1. Simular  `usuarioRepository.findById(99)` para que devuelva `Optional.empty()`.
    2. Ejecutar `foroService.crearTema(requestFalla)`.
    3. Verificar que se lanza la excepción `ResourceNotFoundException`.
    Resultado esperado:
    - Se lanza una excepción `ResourceNotFoundException`.
    - El 'foroRepository.save()' no se ejecuta.

    Explicación del test;
    GIVEN: Configuramos el mock 'usuarioRepository' para que NO encuentre
           al usuario con ID 99 (devuelve Optional.empty()).
    WHEN:  Ejecutamos el metodo 'crearTema' con el ID de usuario inválido,
           y verificamos  que lanza la excepción esperada.
    THEN:  Verificamos que el sistema lanza una 'ResourceNotFoundException' y
           que el metodo 'foroRepository.save()' NUNCA fue invocado.
    */
    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
    void crearTema_whenUsuarioNoExiste_shouldThrowException() {
        // GIVEN
        ForoRequestDTO requestFalla = new ForoRequestDTO("Título válido", "Contenido válido", 99);
        given(usuarioRepository.findById(99)).willReturn(Optional.empty());

        // WHEN
        assertThrows(ResourceNotFoundException.class, () -> {
            foroService.crearTema(requestFalla);
        });

        // THEN
        then(usuarioRepository).should(times(1)).findById(99);
        then(foroRepository).should(never()).save(any(Foro.class));
    }
}





