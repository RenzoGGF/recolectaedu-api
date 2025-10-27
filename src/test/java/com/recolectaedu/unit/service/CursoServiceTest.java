package com.recolectaedu.unit.service;

import com.recolectaedu.controller.MembresiaController;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.repository.CursoRepository;
import com.recolectaedu.service.CursoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

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
    List con atributos atributos:
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
        // GIVEN - Configuramos mocks y datos de prueba
        String institucion = "UNMSM";
        CursoResponse2DTO curso1 = new CursoResponse2DTO(1, "UNMSM", "Cálculo I", "Sistemas", 100L);
        CursoResponse2DTO curso2 = new CursoResponse2DTO(2, "UNMSM", "Física I", "Industrial", 50L);
        List<CursoResponse2DTO> mockListaCursos = List.of(curso1, curso2);

        given(cursoRepository.findCursosPopulares(institucion)).willReturn(mockListaCursos);

        // WHEN - Se ejecuta el metodo a probar
        List<CursoResponse2DTO> resultado = cursoService.findCursosPopulares(institucion);

        // THEN - Se verifican los resultados
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

        // GIVEN - Configuramos el mock para devolver una lista vacía para una institución
        String institucionSinCursos = "InstitucionX";
        given(cursoRepository.findCursosPopulares(institucionSinCursos)).willReturn(Collections.emptyList());

        // WHEN - Se ejecuta el metodo a probar
        List<CursoResponse2DTO> resultado = cursoService.findCursosPopulares(institucionSinCursos);

        // THEN - Se verifican los resultados
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        then(cursoRepository).should(times(1)).findCursosPopulares(institucionSinCursos);
    }

}
