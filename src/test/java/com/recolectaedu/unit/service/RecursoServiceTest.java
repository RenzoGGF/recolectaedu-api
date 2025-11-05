package com.recolectaedu.unit.service;

import com.recolectaedu.dto.request.RecursoArchivoCreateRequestDTO;
import com.recolectaedu.dto.request.RecursoCreateRequestDTO;
import com.recolectaedu.dto.response.AporteConContadoresResponseDTO;
import com.recolectaedu.dto.response.AporteListadoResponseDTO;
import com.recolectaedu.dto.response.RecursoResponse2DTO;
import com.recolectaedu.dto.response.RecursoResponseDTO;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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

    @InjectMocks
    private RecursoService recursoService;
    private Recurso recursoReciente;
    private Recurso recursoAntiguo;
    private Usuario usuarioMock;
    private Perfil perfilMock;
    private Curso cursoMock;

    @BeforeEach
    void setUp() {
        perfilMock = new Perfil();
        perfilMock.setNombre("Autor Test");
        perfilMock.setApellidos("Apellido Test");
        usuarioMock = new Usuario();
        usuarioMock.setId_usuario(1);
        usuarioMock.setPerfil(perfilMock);
        cursoMock = new Curso();
        cursoMock.setId_curso(1);
        cursoMock.setUniversidad("UNMSM");
        cursoMock.setCarrera("Software");
        cursoMock.setNombre("Cálculo I");

        recursoReciente = Recurso.builder()
                .id_recurso(1)
                .titulo("Recurso Reciente")
                .creado_el(LocalDateTime.now())
                .usuario(usuarioMock)
                .curso(cursoMock)
                .tipo(Tipo_recurso.Apuntes)
                .formato(FormatoRecurso.ARCHIVO)
                .build();

        recursoAntiguo = Recurso.builder()
                .id_recurso(2)
                .titulo("Recurso Antiguo")
                .creado_el(LocalDateTime.now().minusDays(1))
                .usuario(usuarioMock)
                .curso(cursoMock)
                .tipo(Tipo_recurso.Ejercicios)
                .formato(FormatoRecurso.TEXTO)
                .build();
    }

    private Usuario createMockUsuario(Integer id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setEmail(email);
        return usuario;
    }

    private Curso createMockCurso(Integer id, String nombre, String universidad, String carrera) {
        Curso curso = new Curso();
        curso.setId_curso(id);
        curso.setNombre(nombre);
        curso.setUniversidad(universidad);
        curso.setCarrera(carrera);
        return curso;
    }

    @Nested
    @DisplayName("US-05: Publicar Recurso")
    class PublicarRecursoTests {

        @Test
        @DisplayName("CP-0501: Publicar archivo PDF válido")
        void crearDesdeArchivo_ValidPdfFile_Success() {
            // Arrange
            Integer userId = 1;
            String university = "UNMSM";
            String career = "Software";
            String courseName = "Cálculo I";

            Usuario mockUser = createMockUsuario(userId, "john@example.com");
            Curso mockCourse = createMockCurso(1, courseName, university, career);

            RecursoArchivoCreateRequestDTO request = new RecursoArchivoCreateRequestDTO(
                    userId, university, career, courseName,
                    "Resumen de Cálculo I", "Resumen completo", FormatoRecurso.ARCHIVO, Tipo_recurso.Apuntes,
                    2024, 1
            );
            MultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[5 * 1024 * 1024]);

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(cursoRepository.findByUniversidadAndCarreraAndNombre(university, career, courseName)).thenReturn(Optional.of(mockCourse));
            when(almacenamientoService.almacenar(mockFile)).thenReturn("some-file-name.pdf");
            when(recursoRepository.save(any(Recurso.class))).thenAnswer(invocation -> {
                Recurso r = invocation.getArgument(0);
                r.setId_recurso(1);
                return r;
            });

            // Act
            RecursoResponseDTO response = recursoService.crearDesdeArchivo(mockFile, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId_recurso()).isEqualTo(1);
            assertThat(response.getTitulo()).isEqualTo(request.titulo());
            assertThat(response.getFormato()).isEqualTo(FormatoRecurso.ARCHIVO);
            assertThat(response.getContenido()).isEqualTo("some-file-name.pdf");

            verify(almacenamientoService).almacenar(mockFile);
            verify(recursoRepository).save(any(Recurso.class));
        }

        @Test
        @DisplayName("CP-0502: Rechazar archivo que excede tamaño")
        void crearDesdeArchivo_FileSizeExceedsLimit_ThrowsException() {
            // Arrange
            Integer userId = 1;
            String university = "UNMSM";
            String career = "Software";
            String courseName = "Algoritmos";

            RecursoArchivoCreateRequestDTO request = new RecursoArchivoCreateRequestDTO(
                    userId, university, career, courseName,
                    "Libro de Algoritmos", "Libro completo", FormatoRecurso.ARCHIVO, Tipo_recurso.Apuntes,
                    2024, 1
            );
            MultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[15 * 1024 * 1024]);

            // Only stub the method that is expected to throw the exception
            when(almacenamientoService.almacenar(mockFile)).thenThrow(new BusinessRuleException("El archivo excede el tamaño máximo permitido de 10MB"));

            // Act & Assert
            assertThatThrownBy(() -> recursoService.crearDesdeArchivo(mockFile, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("El archivo excede el tamaño máximo permitido de 10MB");

            verify(almacenamientoService).almacenar(mockFile);
            verify(usuarioRepository, never()).findById(anyInt());
            verify(cursoRepository, never()).findByUniversidadAndCarreraAndNombre(any(), any(), any());
            verify(recursoRepository, never()).save(any(Recurso.class));
        }

        @Test
        @DisplayName("CP-0503: Publicar enlace válido")
        void crear_ValidLink_Success() {
            // Arrange
            Integer userId = 1;
            String university = "UNMSM";
            String career = "Software";
            String courseName = "Ingeniería de Software";

            Usuario mockUser = createMockUsuario(userId, "john@example.com");
            Curso mockCourse = createMockCurso(1, courseName, university, career);

            RecursoCreateRequestDTO request = new RecursoCreateRequestDTO(
                    userId, university, career, courseName,
                    "Tutorial de Spring Boot", "Video explicativo", "https://youtube.com/video", FormatoRecurso.ENLACE, Tipo_recurso.Practicas,
                    2024, 1
            );

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(cursoRepository.findByUniversidadAndCarreraAndNombre(university, career, courseName)).thenReturn(Optional.of(mockCourse));
            when(recursoRepository.save(any(Recurso.class))).thenAnswer(invocation -> {
                Recurso r = invocation.getArgument(0);
                r.setId_recurso(2);
                return r;
            });

            // Act
            RecursoResponseDTO response = recursoService.crear(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId_recurso()).isEqualTo(2);
            assertThat(response.getContenido()).isEqualTo(request.contenido());
            assertThat(response.getFormato()).isEqualTo(FormatoRecurso.ENLACE);

            verify(recursoRepository).save(any(Recurso.class));
        }

        @Test
        @DisplayName("CP-0504: Publicar texto válido")
        void crear_ValidText_Success() {
            // Arrange
            Integer userId = 1;
            String university = "UNMSM";
            String career = "Software";
            String courseName = "Cálculo I";

            Usuario mockUser = createMockUsuario(userId, "john@example.com");
            Curso mockCourse = createMockCurso(1, courseName, university, career);

            RecursoCreateRequestDTO request = new RecursoCreateRequestDTO(
                    userId, university, career, courseName,
                    "Fórmulas de Derivadas", "Resumen de fórmulas", """
                    1. Derivada de x^n = nx^(n-1)
                    2. Derivada de e^x = e^x
                    3. Derivada de ln(x) = 1/x
                    """, FormatoRecurso.TEXTO, Tipo_recurso.Apuntes,
                    2024, 1
            );

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(cursoRepository.findByUniversidadAndCarreraAndNombre(university, career, courseName)).thenReturn(Optional.of(mockCourse));
            when(recursoRepository.save(any(Recurso.class))).thenAnswer(invocation -> {
                Recurso r = invocation.getArgument(0);
                r.setId_recurso(3);
                return r;
            });

            // Act
            RecursoResponseDTO response = recursoService.crear(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId_recurso()).isEqualTo(3);
            assertThat(response.getContenido()).isEqualTo(request.contenido());
            assertThat(response.getFormato()).isEqualTo(FormatoRecurso.TEXTO);

            verify(recursoRepository).save(any(Recurso.class));
        }

        @Test
        @DisplayName("CP-0505: Rechazar archivo corrupto")
        void crearDesdeArchivo_CorruptedFile_ThrowsException() {
            // Arrange
            Integer userId = 1;
            String university = "UNMSM";
            String career = "Software";
            String courseName = "Programación";

            RecursoArchivoCreateRequestDTO request = new RecursoArchivoCreateRequestDTO(
                    userId, university, career, courseName,
                    "Archivo corrupto", "Descripción", FormatoRecurso.ARCHIVO, Tipo_recurso.Apuntes,
                    2024, 1
            );
            MultipartFile mockFile = new MockMultipartFile("file", "corrupt.pdf", "application/pdf", new byte[0]);

            // Only stub the method that is expected to throw the exception
            when(almacenamientoService.almacenar(mockFile)).thenThrow(new BusinessRuleException("El archivo no es válido o está corrupto."));

            // Act & Assert
            assertThatThrownBy(() -> recursoService.crearDesdeArchivo(mockFile, request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("El archivo no es válido o está corrupto.");

            verify(almacenamientoService).almacenar(mockFile);
            verify(usuarioRepository, never()).findById(anyInt());
            verify(cursoRepository, never()).findByUniversidadAndCarreraAndNombre(any(), any(), any());
            verify(recursoRepository, never()).save(any(Recurso.class));
        }
    }

    @Nested
    @DisplayName("US-08: Historial de Aportes")
    class HistorialDeAportesTests {

        private Usuario createMockUsuario(Integer id, String email) {
            Usuario usuario = new Usuario();
            usuario.setId_usuario(id);
            usuario.setEmail(email);
            return usuario;
        }

        private void mockResourceCounters(Integer resourceId, long positiveVotes,
                                          long negativeVotes) {
            when(resenaRepository.countByRecurso_Id_recursoAndEsPositivo(resourceId, true)).thenReturn(positiveVotes);
            when(resenaRepository.countByRecurso_Id_recursoAndEsPositivo(resourceId, false)).thenReturn(negativeVotes);
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

            mockResourceCounters(1, 5L, 1L);
            mockResourceCounters(2, 8L, 2L);
            mockResourceCounters(3, 0L, 0L);

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

            mockResourceCounters(1, 0L, 0L);
            mockResourceCounters(2, 0L, 0L);

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
        }
    }

    //PRUEBAS DE LA US-12! -------------------------------------------------------------------------------------------

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



    //PRUEBAS DE LA US-09! -------------------------------------------------------------------------------------------

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


    @Test
    @DisplayName("US-09 [Éxito] Debe buscar recursos por palabra clave y curso")
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


    @Test
    @DisplayName("US-09 [Éxito] Debe buscar recursos por palabra clave, curso y tipo")
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


    @Test
    @DisplayName("US-09 [Falla] Debe lanzar IllegalArgumentException si el tipo es inválido")
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

    @Test
    @DisplayName("US-09 [Éxito] Debe buscar sin importar mayúsculas o minúsculas")
    void searchRecursos_whenKeywordCaseInsensitive_shouldReturnMatchingRecursos() {
        // GIVEN
        String keyword = "recurso reciente";

        Object[] repoResult = new Object[]{ recursoReciente, 1L };
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

        then(recursoRepository).should(times(1)).search(
                eq(keyword), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Sort.unsorted())
        );
    }


    //PRUEBAS DE LA US 10! -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("US-10 [Éxito] Debe buscar recursos por filtros combinados (autor y universidad)")
    void searchRecursos_whenAutorAndUniversidadProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String autor = "Autor";
        String universidad = "UNMSM";

        Object[] repoResult1 = new Object[]{ recursoReciente, 0L };
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 0L };
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

    @Test
    @DisplayName("US-10 [Éxito] Debe devolver todos los recursos si no se proveen filtros")
    void searchRecursos_whenNoFiltersProvided_shouldReturnAllRecursos() {
        // GIVEN
        Object[] repoResult1 = new Object[]{ recursoReciente, 0L }; //
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 0L }; //

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

    @Test
    @DisplayName("US-10 [Éxito] Debe buscar recursos por universidad")
    void searchRecursos_whenUniversidadProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String universidad = "UNMSM";

        Object[] repoResult1 = new Object[]{ recursoReciente, 0L };
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 0L };

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

    @Test
    @DisplayName("US-10 [Éxito] Debe buscar recursos por nombre de autor")
    void searchRecursos_whenAutorProvided_shouldReturnMatchingRecursos() {
        // GIVEN
        String autor = "Autor";

        Object[] repoResult1 = new Object[]{ recursoReciente, 0L };
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 0L };

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

    @Test
    @DisplayName("US-10 [Éxito] Debe ordenar por 'recientes' (defecto) si 'ordenarPor' es null")
    void searchRecursos_whenOrdenarPorIsNull_shouldReturnSortedByRecientes() {
        // GIVEN
        Object[] repoResult1 = new Object[]{ recursoAntiguo, 0L };
        Object[] repoResult2 = new Object[]{ recursoReciente, 0L };

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

    @Test
    @DisplayName("US-10 [Éxito] Debe ordenar por 'relevantes' si 'ordenarPor' lo indica")
    void searchRecursos_whenOrdenarPorIsRelevantes_shouldReturnSortedByScore() {
        // GIVEN
        Object[] repoResult1 = new Object[]{ recursoReciente, 5L }; //
        Object[] repoResult2 = new Object[]{ recursoAntiguo, 10L }; //

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
