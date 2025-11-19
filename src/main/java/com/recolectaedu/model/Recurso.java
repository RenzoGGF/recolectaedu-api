package com.recolectaedu.model;

import com.recolectaedu.model.enums.FormatoRecurso;
import com.recolectaedu.model.enums.Periodo;
import com.recolectaedu.model.enums.Tipo_recurso;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Data
@Entity
@Table(name = "Recurso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_recurso;

    @Column(nullable = false, length = 255,columnDefinition = "TEXT")
    private String titulo;

    @Column( nullable = false, length = 255,columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition ="TEXT", nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoRecurso formato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo_recurso tipo;

    @Column(nullable = false)
    private Integer ano;

    @Enumerated(EnumType.ORDINAL) // Guardará 0, 1, 2...
    private Periodo periodo;

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
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BibliotecaRecurso> enBibliotecas;

    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resena> resenas;
}