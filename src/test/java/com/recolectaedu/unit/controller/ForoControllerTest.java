package com.recolectaedu.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recolectaedu.controller.ForoController;
import com.recolectaedu.dto.request.ForoRequestDTO;
import com.recolectaedu.dto.response.ForoResponseDTO;
import com.recolectaedu.service.ForoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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

// Apuntamos al controlador que queremos probar
@WebMvcTest(ForoController.class)
@DisplayName("Pruebas unitarias de Foro Controller")
public class ForoControllerTest {

    @Autowired
    private MockMvc mockMvc; // El cliente HTTP simulado

    @Autowired
    private ObjectMapper objectMapper; // Para convertir DTOs a JSON

    @MockBean // Usamos @MockBean porque estamos en un test de @WebMvcTest
    private ForoService foroService;

    // Datos de prueba comunes
    private ForoRequestDTO requestValido;
    private ForoResponseDTO responseMock;

    @BeforeEach
    void setUp() {
        requestValido = new ForoRequestDTO(
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
    }

    /*
    Escenario 1 (Controller): Creación exitosa (Camino Feliz)
    DADO que el cliente envía un ForoRequestDTO con datos válidos
    CUANDO se llama al endpoint POST /foros
    ENTONCES el sistema llama al foroService.crearTema()
    Y devuelve un estado HTTP 201 (Created) con el DTO de respuesta.

    ID: CP-2001-C
    Historia: US-20
    Escenario: Creación de tema (Controller) exitosa
    */
    @Test
    @DisplayName("Debe devolver 201 Created cuando la solicitud es válida")
    void crearTema_whenRequestValido_shouldReturn201Created() throws Exception {
        // GIVEN
        given(foroService.crearTema(any(ForoRequestDTO.class))).willReturn(responseMock);

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/foros/" + responseMock.id_foro()))
                .andExpect(jsonPath("$.id_foro").value(100))
                .andExpect(jsonPath("$.id_usuario").value(1));

        then(foroService).should(times(1)).crearTema(any(ForoRequestDTO.class));
    }


    /*
    Escenario 2 (Controller): Escenario (datos incorrectos)
    DADO que el cliente envía un ForoRequestDTO con datos inválidos
    (ej. título demasiado corto)
    CUANDO se llama al endpoint POST /foros
    ENTONCES el sistema (Spring Validation) falla la validación
    Y devuelve un estado HTTP 400 (Bad Request) con un mensaje de error.
    Y el foroService.crearTema() NUNCA es llamado.

    ID: CP-2003
    Historia: US-20
    Escenario: Creación de tema (Controller) falla por validación
    */
    @Test
    @DisplayName("Debe devolver 400 Bad Request si el título es muy corto")
    void crearTema_whenTituloCorto_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO(
                "Corto",
                "Este es un contenido de foro válido (20+)",
                1
        );

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                // MODIFICADO: Se añade ".errors" al JSONPath
                .andExpect(jsonPath("$.errors.titulo", containsString("El título debe tener al menos 10 caracteres")));

        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    @Test
    @DisplayName("Debe devolver 400 Bad Request si el contenido es nulo")
    void crearTema_whenContenidoNull_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO(
                "Este es un título válido (10+)",
                null,
                1
        );

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                // MODIFICADO: Se añade ".errors" al JSONPath
                .andExpect(jsonPath("$.errors.contenido", containsString("El contenido no puede ser nulo")));

        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }

    @Test
    @DisplayName("Debe devolver 400 Bad Request si el id_usuario es nulo")
    void crearTema_whenUsuarioNull_shouldReturn400BadRequest() throws Exception {
        // GIVEN
        ForoRequestDTO requestInvalido = new ForoRequestDTO(
                "Este es un título válido (10+)",
                "Este es un contenido de foro válido (20+)",
                null
        );

        // WHEN & THEN
        mockMvc.perform(post("/foros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                // MODIFICADO: Se añade ".errors" al JSONPath
                .andExpect(jsonPath("$.errors.id_usuario", containsString("El usuario tiene que estar registrado")));

        then(foroService).should(never()).crearTema(any(ForoRequestDTO.class));
    }
}