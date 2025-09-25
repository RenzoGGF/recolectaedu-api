package com.recolectaedu.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Perfil")
public class Perfil {

    @Id
    private Integer id_usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String apellidos;

    @Column(nullable = false)
    private Short ciclo;

    @Column(nullable = false, length = 255)
    private String carrera;

    @Column(nullable = false, length = 255)
    private String universidad;
}
