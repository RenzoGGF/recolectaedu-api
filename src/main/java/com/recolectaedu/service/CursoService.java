package com.recolectaedu.service;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    //US-11: Cursos Populares en institucion
    public List<CursoResponse2DTO> findCursosPopulares(String institucion) {
        return cursoRepository.findCursosPopulares(institucion);
    }

    public Page<CursoRankingAportesDTO> getRankingAportes(String universidad, String carrera, Pageable pageable) {
        String universidadParam = StringUtils.hasText(universidad) ? universidad : null;
        String carreraParam = StringUtils.hasText(carrera) ? carrera : null;
        return cursoRepository.rankingPorAportes(universidadParam, carreraParam, pageable);
    }

    //Curso en específico
    public CursoResponse2DTO getCursoById_curso(Integer id_curso) {
        return cursoRepository.findCursoDetailsById(id_curso)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado con id: " + id_curso));
    }

}