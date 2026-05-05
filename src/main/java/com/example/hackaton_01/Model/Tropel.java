package com.example.hackaton_01.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tropels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tropel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 40)
    private String name;

    private String species; // BLOBITO, CHISPA, GRUÑON, DORMILON, GLITCHY

    @Builder.Default
    @Column(nullable = false)
    private String vitalState = "ESTABLE";

    @Builder.Default
    @Column(nullable = false)
    private Integer energyLevel = 80;

    @Builder.Default
    @Column(nullable = false)
    private Integer chaosIndex = 10;

    @Builder.Default
    @Column(nullable = false)
    private Integer mutationStage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "tropel", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TropelSignal> signals = new ArrayList<>();
}