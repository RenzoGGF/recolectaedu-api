package com.recolectaedu.unit.service; // O el paquete donde tengas tus tests

import com.recolectaedu.dto.response.RecursoResponse2DTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.UsuarioRepository; // Importar
import com.recolectaedu.service.IAlmacenamientoService; // Importar
import com.recolectaedu.service.RecursoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de Recurso Service (US-12)")
public class RecursoServiceTest {
    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private IAlmacenamientoService almacenamientoService;

    @InjectMocks
    private RecursoService recursoService;
    private Recurso recursoReciente;
    private Recurso recursoAntiguo;
    private Usuario usuarioMock;
    private Perfil perfilMock;
    private Curso cursoMock;

    @BeforeEach
    void setUp() {
        perfilMock = new Perfil(); //
        perfilMock.setNombre("Autor Test");
        usuarioMock = new Usuario();
        usuarioMock.setId_usuario(1);
        usuarioMock.setPerfil(perfilMock);
        cursoMock = new Curso();
        cursoMock.setId_curso(1);

        recursoReciente = Recurso.builder()
                .id_recurso(1)
                .titulo("Recurso Reciente")
                .creado_el(LocalDateTime.now())
                .usuario(usuarioMock)
                .curso(cursoMock)
                .build();

        recursoAntiguo = Recurso.builder()
                .id_recurso(2)
                .titulo("Recurso Antiguo")
                .creado_el(LocalDateTime.now().minusDays(1))
                .usuario(usuarioMock)
                .curso(cursoMock)
                .build();
    }

    /*
    Escenario 1: Búsqueda de recursos exitosa (filtrado)
    DADO que me encuentro en la sección de recursos de un curso
    CUANDO selecciono el filtro de “Recientes”
    ENTONCES el sistema muestra los recursos pertenecientes al curso.

    ID: CP-1201
    Historia: US-12
    Escenario: Búsqueda de "Recientes" en un curso con recursos.
    Precondiciones:
    - Un 'Curso' existe con 'cursoId = 1'.
    - El 'recursoRepository' tiene 2 recursos para el curso 1
      (recursoReciente, recursoAntiguo).
    Datos de prueba:
    - Integer cursoId = 1
    Pasos:
    1. Simular cursoRepository.existsById(1) para que devuelva true.
    2. Simular recursoRepository.findRecursosRecientesPorCurso(1)
       para que devuelva la lista de 2 recursos.
    3. Ejecutar recursoService.findRecientesByCurso(1).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 2 elementos.
    - El mapeo de DTO debe ser correcto
    Explicación del test;
    GIVEN: Configuramos 'cursoRepository' para que el curso exista
           y 'recursoRepository' para que devuelva una lista de 2 recursos.
    WHEN:  Ejecutamos el metodo findRecientesByCurso.
    THEN:  Verificamos que la lista devuelta no es nula, tiene 2
           elementos y que el mapeo de datos es correcto.
    */

    @Test
    @DisplayName("Debe devolver recursos recientes si el curso existe y tiene recursos")
    void findRecientesByCurso_whenCursoExisteYRecursosPresentes_shouldDevolverLista() {
        // GIVEN - Configuramos el mock
        Integer cursoId = 1;
        List<Recurso> mockLista = List.of(recursoReciente, recursoAntiguo);
        given(cursoRepository.existsById(cursoId)).willReturn(true);
        given(recursoRepository.findRecursosRecientesPorCurso(cursoId)).willReturn(mockLista);

        // WHEN - Se ejecuta el metodo a probar
        List<RecursoResponse2DTO> resultado = recursoService.findRecientesByCurso(cursoId);

        // THEN - Se verifican los resultados
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
        assertThat(resultado.get(0).autorNombre()).isEqualTo("Autor Test"); //
        assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");
        then(cursoRepository).should(times(1)).existsById(cursoId);
        then(recursoRepository).should(times(1)).findRecursosRecientesPorCurso(cursoId);
    }

    /*
    Escenario 2: Búsqueda sin recursos (sin contenido)
    DADO que me encuentro en la sección de recursos de un curso
    CUANDO selecciono el filtro de “Recientes” y no existen recursos
    relacionados al curso
    ENTONCES el sistema muestra un mensaje de sin contenido.

    ID: CP-1202
    Historia: US-12
    Escenario: Búsqueda de "Recientes" en un curso que existe pero no tiene recursos.
    Precondiciones:
    - Un 'Curso' existe con 'cursoId = 2'.
    - El 'recursoRepository' devuelve una lista vacía para el curso 2.
    Datos de prueba:
    - Integer cursoId = 2
    Pasos:
    1. Simular cursoRepository.existsById(2) para que devuelva true.
    2. Simular recursoRepository.findRecursosRecientesPorCurso(2)
       para que devuelva Collections.emptyList().
    3. Ejecutar recursoService.findRecientesByCurso(2).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> vacía.
    Explicación del test;
    GIVEN: Configuramos 'cursoRepository' para que el curso exista
           y 'recursoRepository' para que devuelva una lista vacía.
    WHEN:  Ejecutamos el metodo findRecientesByCurso.
    THEN:  Verificamos que la lista devuelta no es nula y está vacía
    */
    @Test
    @DisplayName("Debe devolver lista vacía si el curso existe pero no tiene recursos")
    void findRecientesByCurso_whenCursoExisteYNoHayRecursos_shouldDevolverListaVacia() {
        // GIVEN - Configuramos el mock para devolver una lista vacía
        Integer cursoId = 2;
        given(cursoRepository.existsById(cursoId)).willReturn(true);
        given(recursoRepository.findRecursosRecientesPorCurso(cursoId)).willReturn(Collections.emptyList());

        // WHEN  - Se ejecuta el metodo a probar
        List<RecursoResponse2DTO> resultado = recursoService.findRecientesByCurso(cursoId);

        // THEN - Se verifican los resultados
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        then(cursoRepository).should(times(1)).existsById(cursoId);
        then(recursoRepository).should(times(1)).findRecursosRecientesPorCurso(cursoId);
    }


    /*
    Escenario 3: Búsqueda en un curso que no existe
    DADO que intento acceder a los recursos recientes
    CUANDO el cursoId proporcionado no existe en la base de datos
    ENTONCES el sistema lanza una excepción de Recurso no encontrado.

    ID: CP-1203
    Historia: US-12
    Escenario: Intento de búsqueda de "Recientes" en un cursoId inválido.
    Precondiciones:
    - No existe ningún 'Curso' con 'cursoId = 99'.
    Datos de prueba:
    - Integer cursoId = 99
    Pasos:
    1. Simular cursoRepository.existsById(99) para que devuelva false.
    2. Ejecutar recursoService.findRecientesByCurso(99).
    Resultado esperado:
    - Se lanza una 'ResourceNotFoundException'.
    - El 'recursoRepository.findRecursosRecientesPorCurso()' NUNCA es llamado.
    Explicación del test;
    GIVEN: Configuramos el mock cursoRepository.existsById(99)
           para que devuelva false.
    WHEN:  Ejecutamos findRecientesByCurso(99) y usamos assertThrows
           para capturar la excepción.
    THEN:  Verificamos que se lanzó ResourceNotFoundException y que
           el 'recursoRepository' NUNCA fue llamado.
    */
    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el curso no existe")
    void findRecientesByCurso_whenCursoNoExiste_shouldLanzarException() {
        // GIVEN
        Integer cursoId = 99;
        given(cursoRepository.existsById(cursoId)).willReturn(false);

        // WHEN
        assertThrows(ResourceNotFoundException.class, () -> {
            recursoService.findRecientesByCurso(cursoId);
        });

        // THEN - Se verifican los resultados
        then(cursoRepository).should(times(1)).existsById(cursoId);
        then(recursoRepository).should(never()).findRecursosRecientesPorCurso(anyInt());
    }
}