package com.recolectaedu.unit.controller; // O tu paquete de tests de controlador

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recolectaedu.config.CorsConfig; //
import com.recolectaedu.config.SecurityConfig; //
import com.recolectaedu.controller.ForoController; //
import com.recolectaedu.dto.request.ForoRequestDTO; //
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.security.JwtAuthenticationFilter; //
import com.recolectaedu.security.JwtUtil; //
import com.recolectaedu.service.ForoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
// Se elimina la importación de CorsConfigurationSource

import java.time.LocalDateTime;
import java.util.List;

// Imports estáticos para las aserciones de MockMvc
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Imports estáticos de BDDito (Mockito)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
// Import para Hamcrest (para verificar el contenido del error)
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ForoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorsConfig.class}) //
@DisplayName("Pruebas unitarias de Foro Controller (Con Seguridad)")
public class ForoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForoService foroService;

    // Mocks para las dependencias de Seguridad
    @MockBean
    private JwtUtil jwtUtil; //
    @MockBean
    private UserDetailsService userDetailsService;

    private ForoRequestDTO requestValido;
    private ForoResponseDTO responseMock;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        requestValido = new ForoRequestDTO( //
                "Este es un título válido (10+)",
                "Este es un contenido de foro válido (20+)",
                1
        );

        responseMock = new ForoResponseDTO(
                100,
                "Este es un título válido (10+)",
                LocalDateTime.now(),
                1
        );

        // Creamos un usuario mock para simular la autenticación
        mockUserDetails = new User(
                "test@usuario.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_FREE"))
        );
    }

    /*
    Escenario (Usuario no autenticado)
    DADO que un usuario no envía un Token JWT válido
    CUANDO intenta crear un tema en POST /foros
    ENTONCES el sistema rechaza la solicitud con un error 403 Forbidden
    Y el foroService NUNCA es llamado.

    ID: CP-2002
    Historia: US-20
    Escenario: Intento de crear tema sin autenticación
    Precondiciones:
    - La configuración de seguridad (`SecurityConfig`) está activa.
    - El endpoint `POST /foros` requiere autenticación (`.anyRequest().authenticated()`).
    Datos de prueba:
    - Un JSON de `ForoRequestDTO` válido.
    Pasos:
    1. Ejecutar `mockMvc.perform(post("/foros")...)` SIN un header 'Authorization'.
    Resultado esperado:
    - La API devuelve un estado HTTP 403 (Forbidden).
    - `foroService.crearTema()` nunca es invocado.
    Explicación del test;
    GIVEN: Un `requestValido` en formato JSON.
    WHEN:  Llamamos al endpoint `POST /foros` sin un token JWT.
    THEN:  Verificamos que la respuesta es `status().isForbidden()`
           y que el servicio nunca fue contactado.
    */
    @Test
    @DisplayName("Debe devolver 403 Forbidden si el usuario no está autenticado")
    void crearTema_whenUsuarioNoAutenticado_shouldReturn403Forbidden() throws Exception {
        // GIVEN: Un request válido, pero sin token

        // WHEN & THEN
        mockMvc.perform(post("/foros") //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isForbidden()); //

        // Verificamos que el servicio NUNCA fue llamado
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }


    /*
    Escenario (éxito):
    DADO que estoy autenticado
    CUANDO completo los datos requeridos (titulo, descripcion)
    Y presiono en “Crear” (llamo a POST /foros)
    ENTONCES el sistema crea el tema (llama a foroService.crearTema)
    Y muestra un mensaje de confirmación (devuelve 201 Created).

    ID: CP-2001
    Historia: US-20
    Escenario: Creación de tema (Controller) exitosa
    Precondiciones:
    - La configuración de seguridad (`SecurityConfig`) está activa.
    - El usuario envía un Token JWT válido en el header 'Authorization'.
    - El `ForoRequestDTO` enviado es válido.
    Datos de prueba:
    - Un `fakeToken` y un `mockUserDetails` para simular la autenticación.
    - `requestValido` (JSON con datos correctos).
    - `responseMock` (el DTO que el servicio devolverá).
    Pasos:
    1. Simular (mock) `jwtUtil` y `userDetailsService` para que validen el token.
    2. Simular (mock) `foroService.crearTema` para que devuelva `responseMock`.
    3. Ejecutar `mockMvc.perform(post("/foros")...)` CON el header 'Authorization: Bearer ...'.
    Resultado esperado:
    - La API devuelve un estado HTTP 201 (Created).
    - El `foroService.crearTema()` es invocado 1 vez.
    Explicación del test;
    GIVEN: Simulamos una autenticación JWT válida y que
           el `foroService` funcionará correctamente.
    WHEN:  Llamamos al endpoint `POST /foros` con un token válido y un body JSON válido.
    THEN:  Verificamos que la respuesta es `status().isCreated()`
           y que el servicio SÍ fue llamado.
    */
    @Test
    @DisplayName("Debe devolver 201 Created cuando el usuario está AUTENTICADO y la solicitud es válida")
    void crearTema_whenAuthAndRequestValido_shouldReturn201Created() throws Exception {
        // GIVEN: Simulamos una autenticación JWT válida
        String fakeToken = "fake-jwt-token";
        given(jwtUtil.validateToken(fakeToken)).willReturn(true); //
        given(jwtUtil.getEmailFromToken(fakeToken)).willReturn("test@usuario.com"); //
        given(userDetailsService.loadUserByUsername("test@usuario.com")).willReturn(mockUserDetails);

        // GIVEN: El servicio funcionará
        given(foroService.crearTema(any(ForoRequestDTO.class))).willReturn(responseMock);

        // WHEN & THEN
        mockMvc.perform(post("/foros") //
                        .header("Authorization", "Bearer " + fakeToken) //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isCreated()) //
                .andExpect(jsonPath("$.id_foro").value(100));

        // Verificamos que el servicio SÍ fue llamado
        then(foroService).should(times(1)).crearTema(any(ForoRequestDTO.class));
    }


    /*
    Escenario (datos incorrectos):
    DADO que estoy autenticado
    CUANDO completo el formulario con datos incorrectos (título corto)
    ENTONCES el sistema no crea un nuevo tema (devuelve 400 Bad Request)
    Y muestra un mensaje específico por cada error.

    ID: CP-2003
    Historia: US-20
    Escenario: Creación de tema (Controller) falla por validación (título corto)
    Precondiciones:
    - El usuario está autenticado (envía un Token JWT válido).
    - El `ForoRequestDTO` enviado es inválido (título < 10 caracteres).
    Datos de prueba:
    - Un `fakeToken` y un `mockUserDetails`.
    - `requestInvalido` (JSON con título "Corto").
    Pasos:
    1. Simular (mock) `jwtUtil` y `userDetailsService` para que validen el token.
    2. Ejecutar `mockMvc.perform(post("/foros")...)` CON el token y el JSON inválido.
    Resultado esperado:
    - La API devuelve un estado HTTP 400 (Bad Request).
    - El JSON de respuesta contiene el mensaje de error de validación.
    - `foroService.crearTema()` NUNCA es invocado.
    Explicación del test;
    GIVEN: Simulamos una autenticación JWT válida, pero un `ForoRequestDTO` inválido.
    WHEN:  Llamamos al endpoint `POST /foros` con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es `status().isBadRequest()`
           y que el servicio NUNCA fue llamado.
    */
    @Test
    @DisplayName("Debe devolver 400 Bad Request si está AUTENTICADO pero el título es corto")
    void crearTema_whenAuthAndTituloCorto_shouldReturn400BadRequest() throws Exception {
        // GIVEN: Simulamos una autenticación JWT válida
        String fakeToken = "fake-jwt-token";
        given(jwtUtil.validateToken(fakeToken)).willReturn(true);
        given(jwtUtil.getEmailFromToken(fakeToken)).willReturn("test@usuario.com");
        given(userDetailsService.loadUserByUsername("test@usuario.com")).willReturn(mockUserDetails);

        // GIVEN: Un DTO con datos inválidos (título corto)
        ForoRequestDTO requestInvalido = new ForoRequestDTO("Corto", "Contenido válido...", 1); //

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .header("Authorization", "Bearer " + fakeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo", containsString("El título debe tener al menos 10 caracteres"))); //

        // Verificamos que el servicio NUNCA fue llamado
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    /*
    ID: CP-2004
    Historia: US-20
    Escenario: Creación de tema (Controller) falla por validación (contenido nulo)
    Explicación del test;
    GIVEN: Simulamos una autenticación JWT válida, pero un `ForoRequestDTO` inválido (contenido nulo).
    WHEN:  Llamamos al endpoint `POST /foros` con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es `status().isBadRequest()`
           y que el servicio NUNCA fue llamado.
    */
    @Test
    @DisplayName("Debe devolver 400 Bad Request si está AUTENTICADO pero el contenido es nulo")
    void crearTema_whenAuthAndContenidoNull_shouldReturn400BadRequest() throws Exception {
        // GIVEN: Simulamos una autenticación JWT válida
        String fakeToken = "fake-jwt-token";
        given(jwtUtil.validateToken(fakeToken)).willReturn(true);
        given(jwtUtil.getEmailFromToken(fakeToken)).willReturn("test@usuario.com");
        given(userDetailsService.loadUserByUsername("test@usuario.com")).willReturn(mockUserDetails);

        // GIVEN: Un DTO con datos inválidos (contenido nulo)
        ForoRequestDTO requestInvalido = new ForoRequestDTO("Este es un título válido (10+)", null, 1); //

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .header("Authorization", "Bearer " + fakeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contenido", containsString("El contenido no puede ser nulo"))); //

        // Verificamos que el servicio NUNCA fue llamado
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }
}