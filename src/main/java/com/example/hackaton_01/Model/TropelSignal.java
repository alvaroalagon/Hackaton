package com.example.hackaton_01.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tropel_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TropelSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderTag;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawContent;

    private String signalType;
    private String severity;
    private String assignedUnit;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    private String status; // RECIBIDA, PROCESANDO, ATENDIDA, ERROR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tropel_id", nullable = false)
    private Tropel tropel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @OneToOne(mappedBy = "signal", cascade = CascadeType.ALL)
    private CareResponse careResponse;

    @OneToMany(mappedBy = "signal", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NotificationLog> notificationLogs = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}