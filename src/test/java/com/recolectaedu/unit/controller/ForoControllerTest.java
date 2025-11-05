package com.recolectaedu.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recolectaedu.config.CorsConfig;
import com.recolectaedu.config.SecurityConfig;
import com.recolectaedu.controller.ForoController;
import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.security.JwtAuthenticationFilter;
import com.recolectaedu.security.JwtUtil;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ForoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorsConfig.class}) //
@DisplayName("Pruebas unitarias de Foro Controller")
public class ForoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ForoService foroService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserDetailsService userDetailsService;




    /*
    Escenario 3: Titulo corto:
    DADO que estoy autenticado
    CUANDO completo el formulario con un titulo menor a 10 caracteres
    ENTONCES el sistema no crea un nuevo tema
    Y muestra un mensaje específico por cada error.

    ID: CP-2003
    Historia: US-20
    Escenario: Creación de tema con título corto
    Precondiciones:
    - El usuario está autenticado.
    - El 'ForoRequestDTO' enviado es inválido por título menor a 10 caracteres.
    Datos de prueba:
    - Un 'fakeToken' y un 'mockUserDetails'.
    - 'requestInvalido'.
    Pasos:
    1. Simular 'jwtUtil' y 'userDetailsService' para que validen el token.
    2. Ejecutar 'mockMvc.perform(post("/foros")...)' CON el token y el JSON inválido.
    Resultado esperado:
    - La API devuelve un estado HTTP 400 Bad Request.
    - El JSON de respuesta contiene el mensaje de error de validación.
    - 'foroService.crearTema()' NUNCA es invocado.
    Explicación del test:
    GIVEN: Simulamos una autenticación JWT válida, pero un 'ForoRequestDTO' inválido.
    WHEN:  Llamamos al endpoint 'POST /foros' con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es 'status().isBadRequest()'
           y que el servicio NUNCA fue llamado.
    */

    @Test
    @WithMockUser
    @DisplayName("F - Debe devolver 400 si está autenticado pero el título es corto")
    void crearTema_whenAuthAndTituloCorto_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO("Corto", "Contenido válido de más de 20 caracteres");

        // WHEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo", containsString("El título debe tener al menos 10 caracteres"))); //

        // THEN
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    /*
    ID: CP-2004
    Historia: US-20
    Escenario 4: Creación de tema por con contenido corto
    Precondiciones:
    - El usuario está autenticado.
    - El 'ForoRequestDTO' enviado es inválido por contenido menor a 20 caracteres.
    Datos de prueba:
    - Un 'fakeToken' y un 'mockUserDetails'.
    - 'requestInvalido'.
    Pasos:
    1. Simular 'jwtUtil' y 'userDetailsService' para que validen el token.
    2. Ejecutar 'mockMvc.perform(post("/foros")...)' CON el token y el JSON inválido.
    Resultado esperado:
    - La API devuelve un estado HTTP 400 Bad Request.
    - El JSON de respuesta contiene el mensaje de error de validación.
    - 'foroService.crearTema()' NUNCA es invocado.
    Explicación del test:
    GIVEN: Simulamos una autenticación JWT válida, pero un 'ForoRequestDTO' inválido con contenido menor a 20.
    WHEN:  Llamamos al endpoint 'POST /foros' con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es 'status().isBadRequest()'
           y que el servicio NUNCA fue llamado.
    */
    @Test
    @WithMockUser
    @DisplayName("F - Debe devolver 400 si está autenticado pero el contenido es corto")
    void crearTema_whenAuthAndContenidoCorto_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO("Este es un título válido", "Corto"); //

        // WHEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contenido", containsString("El contenido debe tener al menos 20 caracteres"))); //

        // THEN
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    /*
    ID: CP-2005
    Historia: US-20
    Escenario: Creación de tema con título nulo

    Precondiciones:
    - El usuario está autenticado.
    - El 'ForoRequestDTO' enviado es inválido por título nulo.
    Datos de prueba:
    - Un 'fakeToken' y un 'mockUserDetails'.
    - 'requestInvalido'.
    Pasos:
    1. Simular 'jwtUtil' y 'userDetailsService' para que validen el token.
    2. Ejecutar 'mockMvc.perform(post("/foros")...)' CON el token y el JSON inválido.
    Resultado esperado:
    - La API devuelve un estado HTTP 400 Bad Request.
    - El JSON de respuesta contiene el mensaje de error de validación.
    - 'foroService.crearTema()' NUNCA es invocado.
    Explicación del test:
    GIVEN: Simulamos una autenticación JWT válida, pero un 'ForoRequestDTO' inválido por título nulo.
    WHEN:  Llamamos al endpoint 'POST /foros' con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es 'status().isBadRequest()'
           y que el servicio NUNCA fue llamado.
    */
    @Test
    @WithMockUser
    @DisplayName("F - Debe devolver 400 si está autenticado pero el título es nulo")
    void crearTema_whenAuthAndTituloNull_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO(null, "Contenido válido de más de 20 caracteres"); //

        // WHEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo", containsString("El título no puede ser nulo"))); //

        // THEN
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    /*
    ID: CP-2006
    Historia: US-20
    Escenario: Creación de tema con contenido nulo
    Precondiciones:
    - El usuario está autenticado.
    - El 'ForoRequestDTO' enviado es inválido por contenido nulo.
    Datos de prueba:
    - Un 'fakeToken' y un 'mockUserDetails'.
    - 'requestInvalido'.
    Pasos:
    1. Simular 'jwtUtil' y 'userDetailsService' para que validen el token.
    2. Ejecutar 'mockMvc.perform(post("/foros")...)' CON el token y el JSON inválido.
    Resultado esperado:
    - La API devuelve un estado HTTP 400 Bad Request.
    - El JSON de respuesta contiene el mensaje de error de validación.
    - 'foroService.crearTema()' NUNCA es invocado.
    Explicación del test;
    GIVEN: Simulamos una autenticación JWT válida, pero un 'ForoRequestDTO' inválido por contenido nulo.
    WHEN:  Llamamos al endpoint 'POST /foros' con un token válido y un body JSON inválido.
    THEN:  Verificamos que la respuesta es 'status().isBadRequest()'
           y que el servicio NUNCA fue llamado.
    */
    @Test
    @WithMockUser
    @DisplayName("F - Debe devolver 400 si está autenticado pero el contenido es nulo")
    void crearTema_whenAuthAndContenidoNull_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO("Este es un título válido (10+)", null); //

        // WHEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contenido", containsString("El contenido no puede ser nulo"))); //

        // THEN
        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

}