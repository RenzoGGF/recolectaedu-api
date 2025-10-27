package com.recolectaedu.unit.repository;

import com.recolectaedu.dto.response.CursoRankingAportesDTO;
import com.recolectaedu.repository.CursoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CursoRepositoryTest {

    @Autowired
    private CursoRepository cursoRepository;

    @Test
    void testRankingPorAportes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CursoRankingAportesDTO> result = cursoRepository.rankingPorAportes(null, null, pageable);
        assertThat(result).isNotNull();
    }
}
