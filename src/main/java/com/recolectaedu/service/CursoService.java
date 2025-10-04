package com.recolectaedu.service;

import com.recolectaedu.dto.response.CursoResponseDTO;
import com.recolectaedu.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;


    public List<CursoResponseDTO> findCursosPopulares() {
        return cursoRepository.findCursosPopulares();
    }

}