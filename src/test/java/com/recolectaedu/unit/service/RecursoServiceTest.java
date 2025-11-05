package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.RecursoPartialUpdateRequestDTO;
import com.recolectaedu.dto.request.RecursoUpdateRequestDTO;
import com.recolectaedu.dto.response.*;
import com.recolectaedu.exception.BusinessRuleException;
import com.recolectaedu.exception.ResourceNotFoundException;
import com.recolectaedu.model.Curso;
import com.recolectaedu.model.Perfil;
import com.recolectaedu.model.Recurso;
import com.recolectaedu.model.Usuario;
import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.OrdenRecurso;
import com.recolectaedu.model.enums.Tipo_recurso;
import com.recolectaedu.repository.ComentarioRepository;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.repository.RecursoRepository;
import com.recolectaedu.repository.ResenaRepository;
import com.recolectaedu.repository.UsuarioRepository;
import com.recolectaedu.service.IAlmacenamientoService;
import com.recolectaedu.service.RecursoService;
import com.recolectaedu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de Recurso Service")
public class RecursoServiceTest {
    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private IAlmacenamientoService almacenamientoService;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private UsuarioService usuarioService;

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
        cursoMock.setUniversidad("UNMSM");

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

    @Nested
    @DisplayName("US-08: Historial de Aportes")
    class HistorialDeAportesTests {

        private Usuario mockUsuario;

        @BeforeEach
        void setUp() {
            mockUsuario = createMockUsuario(1, "john@example.com");
        }

        private Usuario createMockUsuario(Integer id, String email) {
            Usuario usuario = new Usuario();
            usuario.setId_usuario(id);
            usuario.setEmail(email);
            return usuario;
        }

        private void mockResourceCounters(Integer resourceId, long positiveVotes,
                                          long negativeVotes, long comments) {
            when(resenaRepository.countByRecurso_Id_recursoAndEsPositivo(resourceId, true)).thenReturn(positiveVotes);
            when(resenaRepository.countByRecurso_Id_recursoAndEsPositivo(resourceId, false)).thenReturn(negativeVotes);
            // when(comentarioRepository.countByRecursoId(resourceId)).thenReturn(comments);
        }

        @Test
        @DisplayName("Debe listar recursos del usuario con toda la información ordenados por fecha (más reciente primero)")
        void getMyResources_UserWithPublishedResources_ReturnsListOrderedByDateDesc() {
            // Arrange
            Integer userId = 1;
            LocalDateTime now = LocalDateTime.now();

            AporteListadoResponseDTO aporte1 = new AporteListadoResponseDTO(1, "Resumen de Cálculo I", Tipo_recurso.Apuntes, 1, "Cálculo I", "UNMSM", now.minusDays(5), now.minusDays(5));
            AporteListadoResponseDTO aporte2 = new AporteListadoResponseDTO(2, "Tutorial de Spring Boot", Tipo_recurso.Practicas, 1, "Cálculo I", "UNMSM", now.minusDays(2), now.minusDays(2));
            AporteListadoResponseDTO aporte3 = new AporteListadoResponseDTO(3, "Fórmulas de Derivadas", Tipo_recurso.Ejercicios, 1, "Cálculo I", "UNMSM", now, now);

            List<AporteListadoResponseDTO> aportes = Arrays.asList(aporte3, aporte2, aporte1);
            Page<AporteListadoResponseDTO> page = new PageImpl<>(aportes);

            when(usuarioRepository.existsById(userId)).thenReturn(true);
            when(recursoRepository.findAportesByUsuario(eq(userId), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            mockResourceCounters(1, 5L, 1L, 3L);
            mockResourceCounters(2, 8L, 2L, 7L);
            mockResourceCounters(3, 0L, 0L, 0L);

            // Act
             Page<AporteConContadoresResponseDTO> response = recursoService.listarMisAportes(userId, null, null, Pageable.unpaged());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(3);
    
            assertThat(response.getContent().get(0).getTitulo()).isEqualTo("Fórmulas de Derivadas");
            assertThat(response.getContent().get(0).getTipo()).isEqualTo(Tipo_recurso.Ejercicios);
    
            assertThat(response.getContent().get(1).getTitulo()).isEqualTo("Tutorial de Spring Boot");
            assertThat(response.getContent().get(2).getTitulo()).isEqualTo("Resumen de Cálculo I");
    
            AporteConContadoresResponseDTO firstResource = response.getContent().get(0);
            assertThat(firstResource.getTitulo()).isNotNull();
            assertThat(firstResource.getTipo()).isNotNull();
            assertThat(firstResource.getFechaCreacion()).isNotNull();
            assertThat(firstResource.getCursoNombre()).isNotNull();
            assertThat(firstResource.getVotosPositivos()).isEqualTo(0);
            assertThat(firstResource.getVotosNegativos()).isEqualTo(0);
            assertThat(firstResource.getComentarios()).isEqualTo(0);
    
            assertThat(response.getContent().get(0).getFechaCreacion())
                    .isAfter(response.getContent().get(1).getFechaCreacion());
            assertThat(response.getContent().get(1).getFechaCreacion())
                    .isAfter(response.getContent().get(2).getFechaCreacion());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando el usuario no tiene recursos publicados")
        void getMyResources_UserWithoutResources_ReturnsEmptyList() {
            // Arrange
            Integer userId = 1;
            when(usuarioRepository.existsById(userId)).thenReturn(true);
            when(recursoRepository.findAportesByUsuario(eq(userId), any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // Act
            Page<AporteConContadoresResponseDTO> response = recursoService.listarMisAportes(userId, null, null, Pageable.unpaged());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();

            verify(resenaRepository, never()).countByRecurso_Id_recursoAndEsPositivo(anyInt(), anyBoolean());
            // verify(comentarioRepository, never()).countByRecursoId(anyInt());
        }

        @Test
        @DisplayName("Debe filtrar y mostrar solo recursos de tipo Apuntes cuando se aplica ese filtro")
        void getMyResourcesByType_FilterByApuntes_ReturnsOnlyApuntesResources() {
            // Arrange
            Integer userId = 1;
            String filterType = "Apuntes";
            LocalDateTime now = LocalDateTime.now();

            AporteListadoResponseDTO aporte1 = new AporteListadoResponseDTO(1, "Resumen de Cálculo I", Tipo_recurso.Apuntes, 1, "Cálculo I", "UNMSM", now.minusDays(3), now.minusDays(3));
            AporteListadoResponseDTO aporte2 = new AporteListadoResponseDTO(2, "Guía de Programación", Tipo_recurso.Apuntes, 1, "Cálculo I", "UNMSM", now.minusDays(1), now.minusDays(1));
            List<AporteListadoResponseDTO> apuntesAportes = Arrays.asList(aporte2, aporte1);

            when(usuarioRepository.existsById(userId)).thenReturn(true);
            when(recursoRepository.findAportesByUsuario(eq(userId), any(), eq(Tipo_recurso.Apuntes), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(apuntesAportes));

            mockResourceCounters(1, 0L, 0L, 0L);
            mockResourceCounters(2, 0L, 0L, 0L);

            // Act
            Page<AporteConContadoresResponseDTO> response = recursoService.listarMisAportes(userId, null, filterType, Pageable.unpaged());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent()).allMatch(r -> r.getTipo() == Tipo_recurso.Apuntes);
            assertThat(response.getContent().get(0).getTitulo()).isEqualTo("Guía de Programación");
            assertThat(response.getContent().get(1).getTitulo()).isEqualTo("Resumen de Cálculo I");
            
            assertThat(response.getContent().get(0).getFechaCreacion())
                    .isAfter(response.getContent().get(1).getFechaCreacion());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando se aplica filtro y no hay recursos de ese tipo")
        void getMyResourcesByType_FilterWithNoResults_ReturnsEmptyList() {
            // Arrange
            Integer userId = 1;
            String filterType = "Practicas";
            when(usuarioRepository.existsById(userId)).thenReturn(true);
            when(recursoRepository.findAportesByUsuario(eq(userId), any(), eq(Tipo_recurso.Practicas), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // Act
            Page<AporteConContadoresResponseDTO> response = recursoService.listarMisAportes(userId, null, filterType, Pageable.unpaged());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();

            verify(resenaRepository, never()).countByRecurso_Id_recursoAndEsPositivo(anyInt(), anyBoolean());
            // verify(comentarioRepository, never()).countByRecursoId(anyInt());
        }
    }

    @Nested
    @DisplayName("US-12: Ver recursos recientes por curso")
    class RecursosRecientesCurso {
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
        @DisplayName("E - Debe devolver recursos recientes si el curso existe y tiene recursos")
        void findRecientesByCurso_whenCursoExisteYRecursosPresentes_shouldDevolverLista() {
            // GIVEN
            Integer cursoId = 1;
            List<Recurso> mockLista = List.of(recursoReciente, recursoAntiguo);
            given(cursoRepository.existsById(cursoId)).willReturn(true);
            given(recursoRepository.findRecursosRecientesPorCurso(cursoId)).willReturn(mockLista);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.findRecientesByCurso(cursoId);

            // THEN
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
        @DisplayName("E - Debe devolver lista vacía si el curso existe pero no tiene recursos")
        void findRecientesByCurso_whenCursoExisteYNoHayRecursos_shouldDevolverListaVacia() {
            // GIVEN
            Integer cursoId = 2;
            given(cursoRepository.existsById(cursoId)).willReturn(true);
            given(recursoRepository.findRecursosRecientesPorCurso(cursoId)).willReturn(Collections.emptyList());

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.findRecientesByCurso(cursoId);

            // THEN
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
        @DisplayName("F - Debe lanzar ResourceNotFoundException si el curso no existe")
        void findRecientesByCurso_whenCursoNoExiste_shouldLanzarException() {
            // GIVEN
            Integer cursoId = 99;
            given(cursoRepository.existsById(cursoId)).willReturn(false);

            // WHEN
            assertThrows(ResourceNotFoundException.class, () -> {
                recursoService.findRecientesByCurso(cursoId);
            });

            // THEN
            then(cursoRepository).should(times(1)).existsById(cursoId);
            then(recursoRepository).should(never()).findRecursosRecientesPorCurso(anyInt());
        }

    }




    @Nested
    @DisplayName("US-09: Búsqueda por curso / palabra clave / tipo documento")
    class BusquedaSimple {
        /*
    Escenario búsqueda palabra clave:
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
        @DisplayName("E - Debe buscar recursos por palabra clave")
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
        Escenario búsqueda tipo:
        DADO que me encuentro en el buscador de recursos
        CUANDO selecciono un tipo de recurso y presiono en “Buscar”
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
        @DisplayName("E - Debe buscar recursos por tipo")
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
        Escenario búsqueda curso:
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
        @DisplayName("E - Debe buscar recursos por ID de curso")
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
        Escenario busqueda por palabra clave y tipo :
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
        @DisplayName("E - Debe buscar recursos por palabra clave y tipo")
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


    /*
        Escenario busqueda por palabra clave y curso:
        DADO que me encuentro en el buscador de recursos
        CUANDO escribo una palabra clave y el ID del curso y presiono en “Buscar”
        ENTONCES el sistema muestra los recursos que contengan la palabra clave
        y que coincidan con el ID del curso solicitado.

        ID: CP-0905
        Historia: US-09
        Escenario: Búsqueda combinada por palabra clave y ID de curso
        Precondiciones:
        - Un Recurso existe con titulo = "Recurso Reciente" y cursoId = 1.
        - El 'recursoRepository.search()' está configurado para devolver
          este recurso cuando se busca por 'keyword' Y 'cursoId'.
        Datos de prueba:
        - String keyword = "Reciente"
        - Integer cursoId = 1
        Pasos:
        1. Simular recursoRepository.search("Reciente", 1, null, null, null, null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo 'recursoReciente'.
        2. Ejecutar recursoService.searchRecursos("Reciente", 1, null, null, null, null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 1 elemento.
        - El elemento debe ser el DTO de "Recurso Reciente".
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 1 recurso cuando se llame
               con la palabra clave "Reciente" Y el 'cursoId = 1'.
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 1
               elemento y que el servicio llamó al repositorio 1 vez.
        */
        @Test
        @DisplayName("E - Debe buscar recursos por palabra clave y curso")
        void searchRecursos_whenKeywordAndCursoIdProvided_shouldReturnMatchingRecursos() {
            // GIVEN
            String keyword = "Reciente";
            Integer cursoId = 1; //
            Object[] repoResult = new Object[]{ recursoReciente, 1L };
            List<Object[]> mockResultList = new java.util.ArrayList<>(Collections.singletonList(repoResult));

            given(recursoRepository.search(
                    eq(keyword),     // keyword
                    eq(cursoId),     // cursoId
                    isNull(),        // tipoEnum
                    isNull(),        // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    keyword, cursoId, null, null, null, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(0).id_curso()).isEqualTo(cursoId);
            then(recursoRepository).should(times(1)).search(
                    eq(keyword), eq(cursoId), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );
        }


        /*
        Escenario busqueda con palabra clave y curso y tipo:
        DADO que me encuentro en el buscador de recursos
        CUANDO escribo una palabra clave, selecciono el tipo de recurso
        y pongo el ID del curso y presiono en “Buscar”
        ENTONCES el sistema muestra los recursos que contengan la palabra clave
        y que coincidan con el tipo de recursos y curso solicitado.

        ID: CP-0906
        Historia: US-09
        Escenario: Búsqueda combinada por palabra clave, tipo y ID de curso
        Precondiciones:
        - Un Recurso existe con titulo = "Recurso Reciente", cursoId = 1 y tipo = Tipo_recurso.Apuntes.
        - El 'recursoRepository.search()' está configurado para devolver
          este recurso cuando se busca por los tres parámetros.
        Datos de prueba:
        - String keyword = "Reciente"
        - Integer cursoId = 1
        - String tipo = "Apuntes"
        Pasos:
        1. Simular recursoRepository.search("Reciente", 1, Tipo_recurso.Apuntes, null, null, null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo 'recursoReciente'.
        2. Ejecutar recursoService.searchRecursos("Reciente", 1, "Apuntes", null, null, null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 1 elemento.
        - El elemento debe ser el DTO de "Recurso Reciente".
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 1 recurso cuando se llame
               con la palabra clave "Reciente", el 'cursoId = 1' Y el enum Tipo_recurso.Apuntes.
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 1
               elemento y que el servicio llamó al repositorio 1 vez con todos los parámetros.
        */
        @Test
        @DisplayName("E - Debe buscar recursos por palabra clave, curso y tipo")
        void searchRecursos_whenKeywordAndCursoIdAndTipoProvided_shouldReturnMatchingRecursos() {
            // GIVEN
            String keyword = "Reciente";
            Integer cursoId = 1;
            String tipoString = "Apuntes";
            Tipo_recurso tipoEnum = Tipo_recurso.Apuntes;

            Object[] repoResult = new Object[]{ recursoReciente, 1L };
            List<Object[]> mockResultList = new java.util.ArrayList<>(Collections.singletonList(repoResult));

            given(recursoRepository.search(
                    eq(keyword),     // keyword
                    eq(cursoId),     // cursoId
                    eq(tipoEnum),    // tipoEnum
                    isNull(),        // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    keyword, cursoId, tipoString, null, null, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(0).id_curso()).isEqualTo(cursoId);
            assertThat(resultado.get(0).tipo()).isEqualTo(tipoEnum);
            then(recursoRepository).should(times(1)).search(
                    eq(keyword), eq(cursoId), eq(tipoEnum), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );
        }


        /*
        Escenario Busqueda con tipo inválido:
        DADO que me encuentro en el buscador de recursos
        CUANDO escribo el tipo de recurso inválido presiono en “Buscar”
        ENTONCES el sistema muestra un mensaje de argumento incorrecto.

        ID: CP-0907
        Historia: US-09
        Escenario: Búsqueda con un tipo de recurso inválido
        Precondiciones:
        - El enum Tipo_recurso NO contiene el valor "VIDEO".
        Datos de prueba:
        - String tipo = "VIDEO"
        Pasos:
        1. Ejecutar recursoService.searchRecursos(null, null, "VIDEO", null, null, null, null).
        2. Usar assertThrows para verificar que se lanza IllegalArgumentException.
        Resultado esperado:
        - Se lanza una 'IllegalArgumentException'.
        - El 'recursoRepository.search()' NUNCA es llamado.
        Explicación del test;
        GIVEN: Un string de tipo "VIDEO" que no existe en el enum Tipo_recurso.
        WHEN:  Ejecutamos el metodo searchRecursos y usamos assertThrows
               para capturar la excepción.
        THEN:  Verificamos que se lanzó IllegalArgumentException
               y que el 'recursoRepository' NUNCA fue invocado.
        */
        @Test
        @DisplayName("F - Debe lanzar IllegalArgumentException si el tipo es inválido")
        void searchRecursos_whenTipoInvalido_shouldLanzarException() {
            // GIVEN
            String tipoInvalido = "VIDEO";

            // WHEN
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                recursoService.searchRecursos(
                        null, null, tipoInvalido, null, null, null, null
                );
            });
            assertThat(exception.getMessage()).isEqualTo("El tipo de recurso '" + tipoInvalido+ "' no es válido.");

            // THEN
            then(recursoRepository).should(never()).search(
                    any(), any(), any(), any(), any(), any(), any(Sort.class)
            );
        }

        /*
        Escenario Busqueda con mayúsculas y minúsculas:
        DADO que me encuentro en el buscador de recursos
        CUANDO escribo la palabra clave con mayúsculas y presiono “Buscar”
        ENTONCES el sistema muestra el recurso sin importar las mayúsculas o minúsculas.

        ID: CP-0908
        Historia: US-09
        Escenario: Búsqueda sin sensibilidad a mayúsculas o minúsculas
        Precondiciones:
        - Un Recurso existe con titulo = "Recurso Reciente".
        - El 'recursoRepository.search()' está configurado para devolver
          este recurso incluso si la palabra clave se pasa en minúsculas.
        Datos de prueba:
        - String keyword = "recurso reciente"
        Pasos:
        1. Simular (mock) recursoRepository.search("recurso reciente", null, null, null, null, null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo 'recursoReciente'.
        2. Ejecutar recursoService.searchRecursos("recurso reciente", null, null, null, null, null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 1 elemento ("Recurso Reciente").
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               el recurso "Recurso Reciente" cuando se le llame
               con la palabra clave en minúsculas "recurso reciente".
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 1
               elemento, probando que la búsqueda no es sensible a mayúsculas.
        */
        @Test
        @DisplayName("E - Debe buscar sin importar mayúsculas o minúsculas")
        void searchRecursos_whenKeywordCaseInsensitive_shouldReturnMatchingRecursos() {
            // GIVEN
            String keyword = "recurso reciente";

            Object[] repoResult = new Object[]{ recursoReciente, 1L };
            List<Object[]> mockResultList = new java.util.ArrayList<>(Collections.singletonList(repoResult));

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

            then(recursoRepository).should(times(1)).search(
                    eq(keyword), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );
        }
    }




    @Nested
    @DisplayName("US-10: Búsqueda con filtros universidad / relevancia / autor")
    class BusquedaFiltro {
        /*
        Escenario filtros combinados:
        DADO que me encuentro en la sección de búsqueda avanzada
        CUANDO ingreso la universidad y autor deseado y presiono en “Buscar”
        ENTONCES el sistema solo muestra los recursos que cumplan con tales condiciones.

        ID: CP-1001
        Historia: US-10
        Escenario: Búsqueda con filtros combinados autor y universidad
        Precondiciones:
        - Dos Recursos existen asociados al 'cursoId = 1'.
        - El 'cursoMock' (ID 1) tiene 'universidad = "UNMSM"'.
        - Ambos recursos están asociados al 'usuarioMock' (ID 1).
        - El 'perfilMock' ,asociado al usuario 1, tiene 'nombre = "Autor Test"'.
        Datos de prueba:
        - String autor = "Autor"
        - String universidad = "UNMSM"
        Pasos:
        1. Simular recursoRepository.search(null, null, null, "Autor", "UNMSM", null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo ambos recursos.
        2. Ejecutar recursoService.searchRecursos(null, null, null, "Autor", "UNMSM", null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 2 elementos.
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 2 recursos cuando se llame
               con 'autor="Autor"' Y 'universidad="UNMSM"'.
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 2
               elementos y que el servicio llamó al repositorio 1 vez con ambos filtros.
        */
        @Test
        @DisplayName("E - Debe buscar recursos por filtros combinados autor y universidad")
        void searchRecursos_whenAutorAndUniversidadProvided_shouldReturnMatchingRecursos() {
            // GIVEN
            String autor = "Autor";
            String universidad = "UNMSM";

            Object[] repoResult1 = new Object[]{recursoReciente, 0L};
            Object[] repoResult2 = new Object[]{recursoAntiguo, 0L};
            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    eq(autor),       // autorNombre
                    eq(universidad), // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, autor, universidad, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), eq(autor), eq(universidad), isNull(), eq(Sort.unsorted())
            );
        }

    /*
    Escenario busqueda vacía:
    DADO que me encuentro en la sección de búsqueda avanzada
    CUANDO presiono en “Buscar” sin filtros
    ENTONCES el sistema muestra todos los resultados.

    ID: CP-1002
    Historia: US-10
    Escenario: Búsqueda vacía sin filtros
    Precondiciones:
    - El repositorio tiene 2 recursos ('recursoReciente' y 'recursoAntiguo').
    - La consulta search con todos los parámetros null devolverá ambos.
    Datos de prueba:
    - Todos los parámetros del servicio son null.
    Pasos:
    1. Simular recursoRepository.search(null, null, null, null, null, null, Sort.unsorted())
       para que devuelva una lista de Object[] conteniendo ambos recursos.
    2. Ejecutar recursoService.searchRecursos(null, null, null, null, null, null, null).
    Resultado esperado:
    - Una Lista<RecursoResponse2DTO> con 2 elementos, ordenada por fecha (recientes primero).
    Explicación del test;
    GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
           una lista simulada de 2 recursos cuando se llame
           con todos los parámetros null.
    WHEN:  Ejecutamos el metodo searchRecursos.
    THEN:  Verificamos que la lista devuelta no es nula, tiene 2
           elementos, probando que se retornaron todos, y está ordenada por defecto.
    */

        @Test
        @DisplayName("E - Debe devolver todos los recursos si no se proveen filtros")
        void searchRecursos_whenNoFiltersProvided_shouldReturnAllRecursos() {
            // GIVEN
            Object[] repoResult1 = new Object[]{recursoReciente, 0L}; //
            Object[] repoResult2 = new Object[]{recursoAntiguo, 0L}; //

            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    isNull(),        // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, null, null, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );

        }

        /*
        Escenario filtro por universidad
        DADO que me encuentro en la sección de búsqueda avanzada
        CUANDO ingreso en la sección de universidad una universidad presiono en “Buscar”
        ENTONCES el sistema mostrará los recursos de la universidad.

        ID: CP-1003
        Historia: US-10
        Escenario: Búsqueda simple por universidad
        Precondiciones:
        - Dos Recursos existen asociados a un curso de la "UNMSM".
        - El 'recursoRepository.search()' está configurado para devolver
          estos recursos cuando se busca por 'universidad = "UNMSM"'.
        Datos de prueba:
        - String universidad = "UNMSM"
        Pasos:
        1. Simular (mock) recursoRepository.search(null, null, null, null, "UNMSM", null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo ambos recursos.
        2. Ejecutar recursoService.searchRecursos(null, null, null, null, "UNMSM", null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 2 elementos.
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 2 recursos cuando se llame
               únicamente con la 'universidad="UNMSM"'.
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 2
               elementos y que el servicio llamó al repositorio 1 vez.
        */
        @Test
        @DisplayName("E - Debe buscar recursos por universidad")
        void searchRecursos_whenUniversidadProvided_shouldReturnMatchingRecursos() {
            // GIVEN
            String universidad = "UNMSM";

            Object[] repoResult1 = new Object[]{recursoReciente, 0L};
            Object[] repoResult2 = new Object[]{recursoAntiguo, 0L};

            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    isNull(),        // autorNombre
                    eq(universidad), // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, null, universidad, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), isNull(), eq(universidad), isNull(), eq(Sort.unsorted())
            );
        }

        /*
        Escenario filtro por nombre o apellido
        DADO que me encuentro en la sección de búsqueda avanzada
        CUANDO ingreso en la sección de autor el nombre o apellido del autor
        ENTONCES el sistema mostrará los mismos recursos del autor.

        ID: CP-1004
        Historia: US-10
        Escenario: Búsqueda simple por nombre de autor
        Precondiciones:
        - Dos Recursos existen asociados a un usuario.
        - El 'perfil' de ese usuario tiene 'nombre = "Autor Test"'.
        - El 'recursoRepository.search()' está configurado para devolver
          estos recursos cuando se busca por 'autor = "Autor"'.
        Datos de prueba:
        - String autor = "Autor"
        Pasos:
        1. Simular recursoRepository.search(null, null, null, "Autor", null, null, Sort.unsorted())
           para que devuelva una lista de Object[] conteniendo ambos recursos.
        2. Ejecutar recursoService.searchRecursos(null, null, null, "Autor", null, null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 2 elementos.
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 2 recursos cuando se llame
               únicamente con el 'autor="Autor"'.
        WHEN:  Ejecutamos el metodo searchRecursos.
        THEN:  Verificamos que la lista devuelta no es nula, tiene 2
               elementos y que el servicio llamó al repositorio 1 vez.
        */
        @Test
        @DisplayName("E - Debe buscar recursos por nombre de autor")
        void searchRecursos_whenAutorProvided_shouldReturnMatchingRecursos() {
            // GIVEN
            String autor = "Autor";

            Object[] repoResult1 = new Object[]{recursoReciente, 0L};
            Object[] repoResult2 = new Object[]{recursoAntiguo, 0L};

            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    eq(autor),       // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, autor, null, null, null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), eq(autor), isNull(), isNull(), eq(Sort.unsorted())
            );
        }

        /*
        Escenario Busqueda con ordenamiento base o “recientes”
        DADO que me encuentro en la sección de búsqueda avanzada
        CUANDO selecciono ordenamiento nada o recientes
        ENTONCES el sistema mostrará los recursos en orden según creación.

        ID: CP-1005
        Historia: US-10
        Escenario: Búsqueda con ordenamiento por defecto
        Precondiciones:
        - El repositorio tiene 2 recursos ('recursoReciente' y 'recursoAntiguo').
        - El mock del repositorio devolverá la lista en desorden
          ('recursoAntiguo' primero).
        Datos de prueba:
        - Todos los parámetros del servicio son null.
        Pasos:
        1. Simular recursoRepository.search(null, ..., null, Sort.unsorted())
           para que devuelva una lista de Object[] desordenada
           ([recursoAntiguo], [recursoReciente]).
        2. Ejecutar recursoService.searchRecursos(null, ..., null, null).
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 2 elementos.
        - El primer elemento debe ser "Recurso Reciente" (el servicio lo reordenó).
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 2 recursos en desorden (antiguo primero).
        WHEN:  Ejecutamos el metodo searchRecursos con 'ordenarPor' en null.
        THEN:  Verificamos que la lista devuelta está ordenada correctamente,
               probando que el 'else' del 'sort()'
               del servicio funcionó.
        */
        @Test
        @DisplayName("E - Debe ordenar por 'recientes' (defecto) si 'ordenarPor' es null")
        void searchRecursos_whenOrdenarPorIsNull_shouldReturnSortedByRecientes() {
            // GIVEN
            Object[] repoResult1 = new Object[]{recursoAntiguo, 0L};
            Object[] repoResult2 = new Object[]{recursoReciente, 0L};

            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    isNull(),        // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, null, null, null, null // 'ordenarPor' es null
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Reciente");
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Antiguo");

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );
        }

        /*
        Escenario ordenamiento “Relevantes”
        DADO que me encuentro en la sección de búsqueda avanzada
        CUANDO selecciono ordenamiento relevantes
        ENTONCES el sistema mostrará los recursos en orden según valoración.

        ID: CP-1006
        Historia: US-10
        Escenario: Búsqueda con ordenamiento por "relevantes"
        Precondiciones:
        - El repositorio tiene 2 recursos.
        - 'recursoReciente' tiene un score de 5.
        - 'recursoAntiguo' tiene un score de 10.
        - El mock del repositorio devolverá la lista en orden de "recientes".
        Datos de prueba:
        - String ordenarPor = "relevantes"
        Pasos:
        1. Simular recursoRepository.search(null, ..., null, Sort.unsorted())
           para que devuelva una lista de Object[] con scores ([recursoReciente, 5L], [recursoAntiguo, 10L]).
        2. Ejecutar recursoService.searchRecursos(null, ..., null, "relevantes").
        Resultado esperado:
        - Una Lista<RecursoResponse2DTO> con 2 elementos.
        - El primer elemento debe ser "Recurso Antiguo".
        Explicación del test;
        GIVEN: Configuramos 'recursoRepository.search()' para que devuelva
               una lista simulada de 2 recursos con sus scores.
        WHEN:  Ejecutamos el metodo searchRecursos con 'ordenarPor' = "relevantes".
        THEN:  Verificamos que la lista devuelta está ordenada por 'score'
               (relevancia), probando que el 'if ("relevantes")'
               del servicio funcionó.
        */
        @Test
        @DisplayName("E - Debe ordenar por 'relevantes' si 'ordenarPor' lo indica")
        void searchRecursos_whenOrdenarPorIsRelevantes_shouldReturnSortedByScore() {
            // GIVEN
            Object[] repoResult1 = new Object[]{recursoReciente, 5L}; //
            Object[] repoResult2 = new Object[]{recursoAntiguo, 10L}; //

            List<Object[]> mockResultList = new java.util.ArrayList<>(List.of(repoResult1, repoResult2));

            given(recursoRepository.search(
                    isNull(),        // keyword
                    isNull(),        // cursoId
                    isNull(),        // tipoEnum
                    isNull(),        // autorNombre
                    isNull(),        // universidad
                    isNull(),        // calificacionMinima
                    eq(Sort.unsorted()) // sort
            )).willReturn(mockResultList);

            // WHEN
            List<RecursoResponse2DTO> resultado = recursoService.searchRecursos(
                    null, null, null, null, null, null, OrdenRecurso.valueOf("RELEVANTES")
            );

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).titulo()).isEqualTo("Recurso Antiguo"); // El de score 10
            assertThat(resultado.get(1).titulo()).isEqualTo("Recurso Reciente"); // El de score 5

            then(recursoRepository).should(times(1)).search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
            );
        }
    }

    @Test
    @DisplayName("obtenerRecursosMasValoradosPorCurso: retorna lista cuando el curso existe")
    void obtenerMasValorados_Success() {
        Integer cursoId = 10;
        when(cursoRepository.findById(cursoId)).thenReturn(Optional.of(new Curso()));

        RecursoValoradoResponseDTO dto1 = RecursoValoradoResponseDTO.builder()
                .id_recurso(1).titulo("A").votos_utiles(5).votos_no_utiles(1).votos_netos(4).build();
        RecursoValoradoResponseDTO dto2 = RecursoValoradoResponseDTO.builder()
                .id_recurso(2).titulo("B").votos_utiles(3).votos_no_utiles(0).votos_netos(3).build();

        when(recursoRepository.findMasValoradosPorCursoConMetricas(cursoId))
                .thenReturn(List.of(dto1, dto2));

        var result = recursoService.obtenerRecursosMasValoradosPorCurso(cursoId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id_recurso()).isEqualTo(1);
        verify(cursoRepository).findById(cursoId);
        verify(recursoRepository).findMasValoradosPorCursoConMetricas(cursoId);
    }

    @Test
    @DisplayName("obtenerRecursosMasValoradosPorCurso: lanza ResourceNotFound si curso no existe")
    void obtenerMasValorados_NotFound_ThrowsException() {
        Integer cursoId = 999;
        when(cursoRepository.findById(cursoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.obtenerRecursosMasValoradosPorCurso(cursoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Curso no encontrado");

        verify(cursoRepository).findById(cursoId);
        verify(recursoRepository, never()).findMasValoradosPorCursoConMetricas(any());
    }

    @Test
    @DisplayName("obtenerRecursosMasValoradosPorCurso: retorna lista vacía si el curso existe pero no tiene recursos")
    void obtenerMasValorados_CursoExisteSinRecursos_retornaVacio() {
        Integer cursoId = 2;
        when(cursoRepository.findById(cursoId)).thenReturn(Optional.of(new Curso()));
        when(recursoRepository.findMasValoradosPorCursoConMetricas(cursoId)).thenReturn(List.of());

        List<RecursoValoradoResponseDTO> result = recursoService.obtenerRecursosMasValoradosPorCurso(cursoId);

        assertThat(result).isNotNull().isEmpty();
        verify(cursoRepository).findById(cursoId);
        verify(recursoRepository).findMasValoradosPorCursoConMetricas(cursoId);
    }

    // PUT: éxito cuando el autor coincide
    @Test
    @DisplayName("actualizar: éxito cuando el autor coincide")
    void actualizar_Success_WhenOwner() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(autor); // <- autenticado = autor
        when(cursoRepository.findByUniversidadAndCarreraAndNombre("UNI","SIS","ALG"))
                .thenReturn(Optional.of(new Curso()));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoUpdateRequestDTO req = RecursoUpdateRequestDTO.builder()
                .universidad("UNI").carrera("SIS").nombreCurso("ALG")
                .titulo("T").descripcion("D").contenido("C")
                .formato(FormatoRecurso.TEXTO)
                .tipo(com.recolectaedu.model.enums.Tipo_recurso.Apuntes)
                .ano(2024).periodo(1).build();

        RecursoResponseDTO resp = recursoService.actualizar(100, req);

        assertThat(resp).isNotNull();
        verify(recursoRepository).findById(100);
        verify(usuarioService).getAuthenticatedUsuario();
        verify(cursoRepository).findByUniversidadAndCarreraAndNombre("UNI","SIS","ALG");
        verify(recursoRepository).save(any(Recurso.class));
    }

    // PUT: ownership inválido
    @Test
    @DisplayName("actualizar: lanza BusinessRule si ownership inválido")
    void actualizar_InvalidOwnership_Throws() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Usuario otro = new Usuario();  otro.setId_usuario(2);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(otro);

        RecursoUpdateRequestDTO req = RecursoUpdateRequestDTO.builder()
                .universidad("UNI").carrera("SIS").nombreCurso("ALG")
                .titulo("T").descripcion("D").contenido("C")
                .formato(FormatoRecurso.TEXTO)
                .tipo(com.recolectaedu.model.enums.Tipo_recurso.Apuntes)
                .ano(2024).periodo(1).build();

        assertThatThrownBy(() -> recursoService.actualizar(100, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso");

        verify(recursoRepository, never()).save(any());
    }

    // PUT: no autenticado
    @Test
    @DisplayName("actualizar: falla si no autenticado")
    void actualizar_Unauthenticated_Throws() {
        when(recursoRepository.findById(100)).thenReturn(Optional.of(
                Recurso.builder().id_recurso(100).usuario(new Usuario()).formato(FormatoRecurso.TEXTO).build()
        ));
        when(usuarioService.getAuthenticatedUsuario()).thenThrow(new IllegalStateException("No autenticado"));

        RecursoUpdateRequestDTO req = RecursoUpdateRequestDTO.builder()
                .universidad("UNI").carrera("SIS").nombreCurso("ALG")
                .titulo("T").descripcion("D").contenido("C")
                .formato(FormatoRecurso.TEXTO)
                .tipo(com.recolectaedu.model.enums.Tipo_recurso.Apuntes)
                .ano(2024).periodo(1).build();

        assertThatThrownBy(() -> recursoService.actualizar(100, req))
                .isInstanceOf(IllegalStateException.class);

        verify(recursoRepository, never()).save(any());
    }

    // PATCH: éxito cuando el autor coincide
    @Test
    @DisplayName("actualizarParcial: éxito cuando el autor coincide")
    void actualizarParcial_Success_WhenOwner() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(autor);
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoPartialUpdateRequestDTO req = new RecursoPartialUpdateRequestDTO(null, null, null, "Nuevo título", null, null, null, null, null, null);

        RecursoResponseDTO resp = recursoService.actualizarParcial(100, req);

        assertThat(resp).isNotNull();
        verify(usuarioService).getAuthenticatedUsuario();
        verify(recursoRepository).save(any(Recurso.class));
    }

    // PATCH: ownership inválido
    @Test
    @DisplayName("actualizarParcial: lanza BusinessRule si ownership inválido")
    void actualizarParcial_InvalidOwnership_Throws() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Usuario otro = new Usuario();  otro.setId_usuario(2);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(otro);

        RecursoPartialUpdateRequestDTO req = new RecursoPartialUpdateRequestDTO(null, null, null, "x", null, null, null, null, null, null);

        assertThatThrownBy(() -> recursoService.actualizarParcial(100, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso");

        verify(recursoRepository, never()).save(any());
    }

    // PATCH: no autenticado
    @Test
    @DisplayName("actualizarParcial: falla si no autenticado")
    void actualizarParcial_Unauthenticated_Throws() {
        when(recursoRepository.findById(100)).thenReturn(Optional.of(
                Recurso.builder().id_recurso(100).usuario(new Usuario()).formato(FormatoRecurso.TEXTO).build()
        ));
        when(usuarioService.getAuthenticatedUsuario()).thenThrow(new IllegalStateException("No autenticado"));

        RecursoPartialUpdateRequestDTO req = new RecursoPartialUpdateRequestDTO(null, null, null, "x", null, null, null, null, null, null);

        assertThatThrownBy(() -> recursoService.actualizarParcial(100, req))
                .isInstanceOf(IllegalStateException.class);

        verify(recursoRepository, never()).save(any());
    }

    // DELETE: éxito cuando el autor coincide
    @Test
    @DisplayName("eliminar: éxito cuando el autor coincide")
    void eliminar_Success_WhenOwner() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(autor);

        recursoService.eliminar(100);

        verify(recursoRepository).delete(recurso);
    }

    // DELETE: ownership inválido
    @Test
    @DisplayName("eliminar: lanza BusinessRule si ownership inválido")
    void eliminar_InvalidOwnership_Throws() {
        Usuario autor = new Usuario(); autor.setId_usuario(1);
        Usuario otro = new Usuario();  otro.setId_usuario(2);
        Recurso recurso = Recurso.builder().id_recurso(100).usuario(autor).formato(FormatoRecurso.TEXTO).build();

        when(recursoRepository.findById(100)).thenReturn(Optional.of(recurso));
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(otro);

        assertThatThrownBy(() -> recursoService.eliminar(100))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso");

        verify(recursoRepository, never()).delete(any());
    }

    // DELETE: no autenticado
    @Test
    @DisplayName("eliminar: falla si no autenticado")
    void eliminar_Unauthenticated_Throws() {
        when(recursoRepository.findById(100)).thenReturn(Optional.of(
                Recurso.builder().id_recurso(100).usuario(new Usuario()).formato(FormatoRecurso.TEXTO).build()
        ));
        when(usuarioService.getAuthenticatedUsuario()).thenThrow(new IllegalStateException("No autenticado"));

        assertThatThrownBy(() -> recursoService.eliminar(100))
                .isInstanceOf(IllegalStateException.class);

        verify(recursoRepository, never()).delete(any());
    }

    private void setupAuthentication(Usuario usuario) {
        when(usuarioService.getAuthenticatedUsuario()).thenReturn(usuario);
    }
}
