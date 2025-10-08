package com.recolectaedu.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Reseña")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_resena;

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