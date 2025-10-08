package com.recolectaedu.model;

import com.recolectaedu.model.enums.MembresiaStatus;
import com.recolectaedu.model.enums.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "membresia",
        indexes = {
                @Index(name = "idx_membresia_usuario", columnList = "id_usuario"),
                @Index(name = "idx_membresia_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Usuario 1:N Membresia
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "fk_membresia_usuario"))
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembresiaStatus status; // PENDING/ACTIVE/CANCELED/EXPIRED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan; // MONTHLY/ANNUAL

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Column(nullable = false)
    private boolean autoRenew;
}
