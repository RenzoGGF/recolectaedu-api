package com.recolectaedu.unit.service;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.service.CursoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de Curso Service")
public class CursoServiceTest {

    //PONER DEPENDENCIAS DE DIAGRAMAS DE COMPONENTES
    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private CursoService cursoService;

    /*
    Escenario 1: Busqueda con contenido
    DADO que me encuentro en la sección de una institución
    CUANDO selecciono el filtro de “Populares”
    ENTONCES el sistema muestra los cursos pertenecientes a la
    institución ordenados por popularidad.

    ID: CP-1101
    Historia: US-11
    Escenario: (Busqueda con contenido)
    Precondiciones:
    - El repositorio tiene 2 cursos para la institución "UNMSM".
    - Curso "Cálculo I" (100 aportes)
    - Curso "Física I" (50 aportes)
    Datos de prueba:
    - String institucion = "UNMSM"
    Pasos:
    1. Ejecutar cursoService.findCursosPopulares("UNMSM")
    Resultado esperado:
    - Una Lista<CursoResponse2DTO> con 2 elementos.
    - El primer elemento debe ser "Cálculo I" (el de 100 aportes).
    - El segundo elemento debe ser "Física I" (el de 50 aportes).
    List con atributos:
        Integer id_curso,
        String universidad,
        String nombre,
        String carrera,
        Long totalRecursos

    Explicación del test;
    GIVEN: Configuramos un mock del CursoRepository para que,
           cuando se le pida buscar por "UNMSM", devuelva una
           lista simulada de 2 cursos.
    WHEN:  Ejecutamos el metodo cursoService.findCursosPopulares("UNMSM").
    THEN:  Verificamos que la lista devuelta no es nula, tiene 2
           elementos, está ordenada (Cálculo I primero) y que el
           repositorio fue llamado 1 vez.

    */

    @Test
    @DisplayName("Debe devolver cursos populares para una institución específica")
    void findCursosPopulares_whenInstitucionHasCursos_shouldReturnListaOrdenada() {
        // GIVEN
        String institucion = "UNMSM";
        CursoResponse2DTO curso1 = new CursoResponse2DTO(1, "UNMSM", "Cálculo I", "Sistemas", 100L);
        CursoResponse2DTO curso2 = new CursoResponse2DTO(2, "UNMSM", "Física I", "Industrial", 50L);
        List<CursoResponse2DTO> mockListaCursos = List.of(curso1, curso2);

        given(cursoRepository.findCursosPopulares(institucion)).willReturn(mockListaCursos);

        // WHEN
        List<CursoResponse2DTO> resultado = cursoService.findCursosPopulares(institucion);

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).totalRecursos()).isEqualTo(100L);
        assertThat(resultado.get(0).universidad()).isEqualTo(institucion);
        then(cursoRepository).should(times(1)).findCursosPopulares(institucion);
    }

    /*
    Escenario 2: Busqueda sin contenido
    DADO que me encuentro en la sección de una institución
    CUANDO selecciono el filtro de “Populares” y la institución no
    cuenta con cursos
    ENTONCES el sistema muestra un mensaje sin contenido.

    ID: CP-1102
    Historia: US-11
    Escenario: Búsqueda de cursos populares sin resultados
    Precondiciones:
    - El repositorio no tiene ningún curso para la
      institución "InstitucionX".
    Datos de prueba:
    - String institucion = "InstitucionX"
    Pasos:
    1. Ejecutar cursoService.findCursosPopulares("InstitucionX")
    Resultado esperado:
    - Una Lista<CursoResponse2DTO> vacía.

    Explicación del test;
    GIVEN: Configuramos un mock del CursoRepository para que,
           cuando se le pida buscar por "InstitucionX", devuelva
           una lista vacía.
    WHEN:  Ejecutamos el metodo cursoService.findCursosPopulares("InstitucionX").
    THEN:  Verificamos que la lista devuelta no es nula, está vacía
           y que el repositorio fue llamado 1 vez.
    */

    @Test
    @DisplayName("Debe devolver una lista vacía si la institución no tiene cursos")
    void findCursosPopulares_whenInstitucionHasNoCursos_shouldReturnListaVacia() {

        // GIVEN
        String institucionSinCursos = "InstitucionX";
        given(cursoRepository.findCursosPopulares(institucionSinCursos)).willReturn(Collections.emptyList());

        // WHEN
        List<CursoResponse2DTO> resultado = cursoService.findCursosPopulares(institucionSinCursos);

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        then(cursoRepository).should(times(1)).findCursosPopulares(institucionSinCursos);
    }

    // --- Tests para US-18: Ranking de Cursos con Más Aportes ---

    @Test
    @DisplayName("US-18 Ranking con datos: Debe devolver ranking ordenado por aportes")
    void getRankingAportes_cuandoExistenCursos_debeDevolverRankingOrdenado() {
        // Arrange
        CursoRankingAportesDTO curso1 = new CursoRankingAportesDTO(3, "Base de Datos", "PUCP", "Ingeniería Informática", 85L);
        CursoRankingAportesDTO curso2 = new CursoRankingAportesDTO(5, "Física I", "UPC", "Ingeniería", 85L);
        CursoRankingAportesDTO curso3 = new CursoRankingAportesDTO(1, "Cálculo I", "UPC", "Ciencias de la Computación", 80L);
        CursoRankingAportesDTO curso4 = new CursoRankingAportesDTO(2, "Ingeniería de Software", "UPC", "Ingeniería de Software", 65L);
        CursoRankingAportesDTO curso5 = new CursoRankingAportesDTO(4, "Algoritmos", "UPC", "Ciencias de la Computación", 55L);

        List<CursoRankingAportesDTO> rankingList = Arrays.asList(curso1, curso2, curso3, curso4, curso5);
        Pageable pageable = PageRequest.of(0, 20);
        Page<CursoRankingAportesDTO> rankingPage = new PageImpl<>(rankingList, pageable, rankingList.size());

        when(cursoRepository.rankingPorAportes(any(), any(), any(Pageable.class))).thenReturn(rankingPage);

        // Act
        Page<CursoRankingAportesDTO> resultado = cursoService.getRankingAportes(null, null, pageable);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(5);
        // Se verifica el orden descendente de los aportes
        assertThat(resultado.getContent().get(0).getAportesCount()).isEqualTo(85L);
        assertThat(resultado.getContent().get(2).getAportesCount()).isEqualTo(80L);
        assertThat(resultado.getContent().get(4).getAportesCount()).isEqualTo(55L);
    }

    @Test
    @DisplayName("US-18 Sin cursos: Debe devolver lista vacía si no hay cursos con aportes")
    void getRankingAportes_cuandoNoExistenCursos_debeDevolverPaginaVacia() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<CursoRankingAportesDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(cursoRepository.rankingPorAportes(any(), any(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<CursoRankingAportesDTO> resultado = cursoService.getRankingAportes(null, null, pageable);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("US-18 Empate en aportes: Debe ordenar alfabéticamente por nombre")
    void getRankingAportes_cuandoHayEmpate_debeOrdenarAlfabeticamente() {
        // Arrange
        // Nota: Este test unitario simula la respuesta correcta del repositorio
        // para verificar que el servicio la devuelve sin alterarla. La validación
        // de la lógica de la query JPQL se realiza en una prueba de integración.
        CursoRankingAportesDTO curso_BD = new CursoRankingAportesDTO(3, "BD301", "PUCP", "Informática", 50L);
        CursoRankingAportesDTO curso_FIS = new CursoRankingAportesDTO(2, "FIS101", "UPC", "Física", 50L);
        CursoRankingAportesDTO curso_MAT = new CursoRankingAportesDTO(1, "MAT203", "UPC", "Matemáticas", 50L);
        CursoRankingAportesDTO curso_ALG = new CursoRankingAportesDTO(4, "ALG102", "UPC", "Computación", 30L);

        // Simulamos el orden que debería devolver la query corregida para que el test pase
        List<CursoRankingAportesDTO> rankingList = Arrays.asList(curso_BD, curso_FIS, curso_MAT, curso_ALG);
        Pageable pageable = PageRequest.of(0, 20);
        Page<CursoRankingAportesDTO> rankingPage = new PageImpl<>(rankingList, pageable, rankingList.size());

        when(cursoRepository.rankingPorAportes(any(), any(), any(Pageable.class))).thenReturn(rankingPage);

        // Act
        Page<CursoRankingAportesDTO> resultado = cursoService.getRankingAportes(null, null, pageable);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(4);
        // Se verifica que los cursos con el mismo número de aportes estén ordenados alfabéticamente
        assertThat(resultado.getContent().stream().map(CursoRankingAportesDTO::getNombre))
            .as("El orden alfabético para los cursos empatados debe ser BD301, FIS101, MAT203")
            .containsExactly("BD301", "FIS101", "MAT203", "ALG102");
    }
}
