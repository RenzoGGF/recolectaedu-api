package com.recolectaedu.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Biblioteca_recurso")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BibliotecasRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_biblioteca_recurso;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime agregado_el;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_biblioteca", nullable = false)
    private Biblioteca biblioteca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;
}