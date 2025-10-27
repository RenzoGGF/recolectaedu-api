package com.recolectaedu.service;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.dto.response.CursoResponse2DTO;
import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.repository.CursoRepository;
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


    public List<CursoResponse2DTO> findCursosPopulares() {
        return cursoRepository.findCursosPopulares();
    }

    public Page<CursoRankingAportesDTO> getRankingAportes(String universidad, String carrera, Pageable pageable) {
        String universidadParam = StringUtils.hasText(universidad) ? universidad : null;
        String carreraParam = StringUtils.hasText(carrera) ? carrera : null;
        return cursoRepository.rankingPorAportes(universidadParam, carreraParam, pageable);
    }

}