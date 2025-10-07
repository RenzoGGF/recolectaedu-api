package com.recolectaedu.dto.response;

public class CursoRankingAportesDTO {

    private Integer idCurso;
    private String nombre;
    private String universidad;
    private String carrera;
    private Long aportesCount;

    public CursoRankingAportesDTO(Integer idCurso, String nombre, String universidad, String carrera, Long aportesCount) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.universidad = universidad;
        this.carrera = carrera;
        this.aportesCount = aportesCount;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUniversidad() {
        return universidad;
    }

    public String getCarrera() {
        return carrera;
    }

    public Long getAportesCount() {
        return aportesCount;
    }
}
