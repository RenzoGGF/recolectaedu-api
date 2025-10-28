package com.recolectaedu.unit.service; // O el paquete donde tengas tus tests

import com.recolectaedu.dto.response.RecursoResponse2DTO;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
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
import org.springframework.data.domain.Sort;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

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
        perfilMock.setApellidos("Apellido Test");
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
                .tipo(Tipo_recurso.Apuntes) //
                .formato(FormatoRecurso.ARCHIVO)
                .build();

        recursoAntiguo = Recurso.builder()
                .id_recurso(2)
                .titulo("Recurso Antiguo")
                .creado_el(LocalDateTime.now().minusDays(1))
                .usuario(usuarioMock)
                .curso(cursoMock)
                .tipo(Tipo_recurso.Ejercicios) //
                .formato(FormatoRecurso.TEXTO)
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

    /*
    Escenario (búsqueda palabra clave):
    DADO que me encuentro en el buscador de recursos
    CUANDO escribo una palabra clave y presiono en “Buscar”
    ENTONCES se muestran los recursos que contengan la palabra clave.

    ID: CP-0901
    Historia: US-09
    Escenario: Búsqueda simple por palabra clave
    Precondiciones:
    - Un Recurso existe con titulo = "Recurso Reciente".
    - El 'recursoRepository.search()' está configurado para devolver
      una lista que contiene este recurso cuando se busca por "rec".
    Datos de prueba:
    - String keyword = "rec"
    Pasos:
    1. Simular recursoRepository.search("rec", null, null, null, null, null, Sort.unsorted())
       para que devuelva una lista de Object[] conteniendo el recurso.
    2. Ejecutar recursoService.searchRecursos("rec", null, null, null, null, null, null).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 1 elemento.
    - El elemento debe ser el DTO de "Recurso Reciente".
    Explicación del test;
    GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
           una lista simulada de 1 recurso
           cuando se llame con la palabra clave "rec".
    WHEN:  Ejecutamos el metodo searchRecursos.
    THEN:  Verificamos que la lista devuelta no es nula, tiene 1
           elemento y que el servicio llamó al repositorio 1 vez.
    */

    @Test
    @DisplayName("US-09 Debe buscar recursos por palabra clave")
    void searchRecursos_whenKeywordProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String keyword = "rec";
        Object[] repoResult = new Object[]{ recursoReciente, 1L }; // Recurso y score)
        List<Object[]> mockResultList = Collections.singletonList(repoResult);
        given(recursoRepository.search(
                eq(keyword),     // keyword
                isNull(),        // cursoId
                isNull(),        // tipoEnum
                isNull(),        // autorNombre
                isNull(),        // universidad
                isNull(),        // calificacionMinima
                eq(Sort.unsorted()) // sort
        )).willReturn(mockResultList);

        // WHEN
        List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                keyword, null, null, null, null, null, null
        );

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
        assertThat(resultado.get(0).autorNombre()).isEqualTo("Autor Test");

        then(recursoRepository).should(times(1)).search(
                eq(keyword), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
        );
    }



    /*
    Escenario (búsqueda tipo):
    DADO que me encuentro en el buscador de recursos
    CUANDO escribo un tipo de recurso y presiono en “Buscar”
    ENTONCES se muestran los recursos que sean de ese tipo.

    ID: CP-0902
    Historia: US-09
    Escenario: Búsqueda simple por tipo de recurso
    Precondiciones:
    - Un Recurso existe con tipo = Tipo_recurso.Apuntes (recursoReciente).
    - El 'recursoRepository.search()' está configurado para devolver
      una lista que contiene este recurso cuando se busca por ese tipo.
    Datos de prueba:
    - String tipo = "Apuntes"
    Pasos:
    1. Simular (mock) recursoRepository.search(null, null, Tipo_recurso.Apuntes, null, null, null, Sort.unsorted())
       para que devuelva una lista de Object[] conteniendo 'recursoReciente'.
    2. Ejecutar recursoService.searchRecursos(null, null, "Apuntes", null, null, null, null).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 1 elemento.
    - El elemento debe ser el DTO de "Recurso Reciente" y tener el tipo Apuntes.
    Explicación del test;
    GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
           una lista simulada de 1 recurso cuando se llame con el enum
           Tipo_recurso.Apuntes.
    WHEN:  Ejecutamos el metodo searchRecursos, pasándole el String "Apuntes".
    THEN:  Verificamos que la lista devuelta no es nula, tiene 1
           elemento y que el servicio llamó al repositorio con el enum correcto.
    */
    @Test
    @DisplayName("US-09 [Éxito] Debe buscar recursos por tipo")
    void searchRecursos_whenTipoProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String tipoString = "Apuntes";
        Tipo_recurso tipoEnum = Tipo_recurso.Apuntes;

        Object[] repoResult = new Object[]{ recursoReciente, 1L };
        List<Object[]> mockResultList = Collections.singletonList(repoResult);
        given(recursoRepository.search(
                isNull(),        // keyword
                isNull(),        // cursoId
                eq(tipoEnum),    // tipoEnum
                isNull(),        // autorNombre
                isNull(),        // universidad
                isNull(),        // calificacionMinima
                eq(Sort.unsorted()) // sort
        )).willReturn(mockResultList);

        // WHEN
        List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                null, null, tipoString, null, null, null, null
        );

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
        assertThat(resultado.get(0).tipo()).isEqualTo(tipoEnum);
        then(recursoRepository).should(times(1)).search(
                isNull(), isNull(), eq(tipoEnum), isNull(), isNull(), isNull(), eq(Sort.unsorted())
        );
    }

    /*
    Escenario (búsqueda curso):
    DADO que me encuentro en el buscador de recursos
    CUANDO escribo un ID del curso y presiono en “Buscar”
    ENTONCES se muestran los recursos que contenga ese curso.

    ID: CP-0903
    Historia: US-09
    Escenario: Búsqueda simple por ID de curso
    Precondiciones:
    - Dos Recursos existen asociados al 'cursoId = 1'.
    - El 'recursoRepository.search()' está configurado para devolver
      una lista con ambos recursos cuando se busca por 'cursoId = 1'.
    Datos de prueba:
    - Integer cursoId = 1
    Pasos:
    1. Simular recursoRepository.search(null, 1, null, null, null, null, Sort.unsorted())
       para que devuelva una lista de Object[] conteniendo ambos recursos.
    2. Ejecutar recursoService.searchRecursos(null, 1, null, null, null, null, null).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 2 elementos.
    Explicación del test;
    GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
           una lista simulada de 2 recursos cuando se llame
           únicamente con el 'cursoId = 1'.
    WHEN:  Ejecutamos el metodo searchRecursos.
    THEN:  Verificamos que la lista devuelta no es nula, tiene 2
           elementos y que el servicio llamó al repositorio 1 vez.
    */
    @Test
    @DisplayName("US-09 [Éxito] Debe buscar recursos por ID de curso")
    void searchRecursos_whenCursoIdProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        Integer cursoId = 1;
        Object[] repoResult1 = new Object[]{ recursoReciente, 0L };
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 0L };
        List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));
        given(recursoRepository.search(
                isNull(),        // keyword
                eq(cursoId),     // cursoId
                isNull(),        // tipoEnum
                isNull(),        // autorNombre
                isNull(),        // universidad
                isNull(),        // calificacionMinima
                eq(Sort.unsorted()) // sort
        )).willReturn(mockResultList);

        // WHEN
        List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                null, cursoId, null, null, null, null, null
        );

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
        assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");
        then(recursoRepository).should(times(1)).search(
                isNull(), eq(cursoId), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
        );
    }


    /*
    Escenario (palabra clave y tipo) :
    DADO que me encuentro en el buscador de recursos
    CUANDO escribo una palabra clave y selecciono el tipo de recurso y presiono en “Buscar”
    ENTONCES el sistema muestra los recursos que contengan la palabra clave
    y que coincidan con el tipo de recursos solicitado.

    ID: CP-0904
    Historia: US-09
    Escenario: Búsqueda combinada por palabra clave y tipo
    Precondiciones:
    - Un Recurso existe con titulo = "Recurso Reciente" y tipo = Tipo_recurso.Apuntes.
    - El 'recursoRepository.search()' está configurado para devolver
      este recurso cuando se busca por 'keyword' Y 'tipo'.
    Datos de prueba:
    - String keyword = "Reciente"
    - String tipo = "Apuntes"
    Pasos:
    1. Simular recursoRepository.search("Reciente", null, Tipo_recurso.Apuntes, null, null, null, Sort.unsorted())
       para que devuelva una lista de Object[] conteniendo 'recursoReciente'.
    2. Ejecutar recursoService.searchRecursos("Reciente", null, "Apuntes", null, null, null, null).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 1 elemento.
    - El elemento debe ser el DTO de "Recurso Reciente".
    Explicación del test;
    GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
           una lista simulada de 1 recurso cuando se llame
           con la palabra clave "Reciente" Y el enum Tipo_recurso.Apuntes.
    WHEN:  Ejecutamos el metodo searchRecursos.
    THEN:  Verificamos que la lista devuelta no es nula, tiene 1
           elemento y que el servicio llamó al repositorio 1 vez.
    */
    @Test
    @DisplayName("US-09 [Éxito] Debe buscar recursos por palabra clave y tipo")
    void searchRecursos_whenKeywordAndTipoProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String keyword = "Reciente";
        String tipoString = "Apuntes";
        Tipo_recurso tipoEnum = Tipo_recurso.Apuntes; //

        Object[] repoResult = new Object[]{ recursoReciente, 1L }; // Recurso y score
        List<Object[]> singletonList = Collections.singletonList(repoResult);
        List<Object[]> mockResultList = new java.util.ArrayList<>(singletonList);

        given(recursoRepository.search(
                eq(keyword),     // keyword
                isNull(),        // cursoId
                eq(tipoEnum),    // tipoEnum
                isNull(),        // autorNombre
                isNull(),        // universidad
                isNull(),        // calificacionMinima
                eq(Sort.unsorted()) // sort
        )).willReturn(mockResultList);

        // WHEN
        List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                keyword, null, tipoString, null, null, null, null
        );

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
        assertThat(resultado.get(0).tipo()).isEqualTo(tipoEnum);
        then(recursoRepository).should(times(1)).search(
                eq(keyword), isNull(), eq(tipoEnum), isNull(), isNull(), isNull(), eq(Sort.unsorted())
        );
    }

    // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA


}