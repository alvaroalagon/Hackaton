package com.example.hackaton_01.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "care_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false, unique = true)
    private TropelSignal signal;

    private String responseCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt;
}