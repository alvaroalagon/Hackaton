package com.example.hackaton_01.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private TropelSignal signal;

    private String recipientEmail;
    private String subject;
    private String notifStatus; // SENT, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant sentAt;
    private Instant createdAt;
}