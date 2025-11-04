package com.recolectaedu.unit.controller;

import com.recolectaedu.controller.UsuarioController;
import com.recolectaedu.security.JwtAuthenticationFilter;
import com.recolectaedu.security.JwtUtil;
import com.recolectaedu.service.RecursoService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // IGNORAR ERROR
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false) // no se ejecutan filtros de seguridad
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean // IGNORAR ERROR
    private UsuarioService usuarioService;

    @MockBean
    private RecursoService recursoService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    // US-1: Registro de usuario: Datos inválidos
    @Test
    @DisplayName("POST /usuarios/register: body inválido -> 400 y ProblemDetail 'Datos inválidos'")
    void register_invalido_400() throws Exception {
        // Arrange: email inválido, password muy corta
        String invalidJson = """
        {
          "email": "no-es-email",
          "password": "123",
          "rol": "ROLE_FREE",
          "perfil": {}
        }
        """;

        // Act + Assert
        mvc.perform(post("/usuarios/register")   // si en test no aplica context-path, esto está bien
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"));

        // El service no debería ejecutarse cuando falla la validación
        verifyNoInteractions(usuarioService);
    }
}
