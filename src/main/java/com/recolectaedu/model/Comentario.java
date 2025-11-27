package com.recolectaedu.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Entity
@Table(name = "Comentario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_comentario;

    @Column(nullable = false)
    private String contenido;

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
    @JoinColumn(name = "id_foro", nullable = false)
    private Foro foro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comentario_padre")
    private Comentario comentario_padre;

    @OneToMany(mappedBy = "comentario_padre")
    private List<Comentario> respuestas;
}