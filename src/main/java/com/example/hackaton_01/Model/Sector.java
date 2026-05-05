package com.example.hackaton_01.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sectorCode;

    private String climate; // PIXEL_FOREST, NEON_CAVE, CLOUD_AQUARIUM, RETRO_ARCADE

    @Column(nullable = false)
    private Integer capacity;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentLoad = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer stabilityLevel = 100;

    private Instant createdAt;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tropel> tropeles = new ArrayList<>();
}