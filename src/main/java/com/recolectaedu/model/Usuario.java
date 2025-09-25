package com.recolectaedu.model;


import com.recolectaedu.model.enums.Rol;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password_hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
}