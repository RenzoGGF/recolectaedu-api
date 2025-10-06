package com.recolectaedu.model;

import com.recolectaedu.model.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // Relación 1:1 con Perfil
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private Perfil perfil;

    // Metodo helper para mantener consistencia
    public void attachPerfil(Perfil perfil) {
        this.perfil = perfil;
        if (perfil != null) {
            perfil.setUsuario(this);
        }
    }
}