package com.recolectaedu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    private Integer id_usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_perfil_usuario"))
    private Usuario usuario;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String apellidos;

    @Column(nullable = false, length = 255)
    private String universidad;

    @Column(nullable = false, length = 255)
    private String carrera;

    @Column(nullable = false)
    private String ciclo;
}
