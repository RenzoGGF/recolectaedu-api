package com.recolectaedu.model;


import com.recolectaedu.model.enums.Plan;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Membresía")
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_membresía;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan tipo;

    @Column(nullable = false)
    private Boolean es_activo;

    @Column(nullable = false)
    private LocalDateTime empieza_el;

    @Column(nullable = false)
    private LocalDateTime termina_el;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}