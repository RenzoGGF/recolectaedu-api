package com.recolectaedu.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Reseña")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_reseña;

    @Column(name = "Título", nullable = false, length = 255)
    private String titulo;

    @Lob
    private String contenido;

    @Column(nullable = false)
    private Boolean es_positivo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creado_el;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime actualizado_el;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;
}