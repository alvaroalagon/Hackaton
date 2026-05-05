package com.example.hackaton_01.Service;

import com.example.hackaton_01.Event.TropelSignalCreatedEvent;
import com.example.hackaton_01.Model.NotificationLog;
import com.example.hackaton_01.Model.TropelSignal;
import com.example.hackaton_01.Repository.NotificationLogRepository;
import com.example.hackaton_01.Repository.TropelSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationListenerService {

    private final TropelSignalRepository signalRepository;
    private final NotificationLogRepository logRepository;
    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional // Obligatorio según tu Readme para guardar el Log
    public void handleSignalCreatedEvent(TropelSignalCreatedEvent event) {
        TropelSignal signal = signalRepository.findById(event.signalId()).orElse(null);
        if (signal == null) return;

        signal.setStatus("PROCESANDO");
        signalRepository.save(signal);

        String recipient = signal.getGuardian().getNotificationEmail();
        String subject = "ALERTA TROPELCARE: " + signal.getSignalType();
        String text = "El Tropel " + signal.getTropel().getName() + " requiere intervención.\n" +
                "Unidad asignada: " + signal.getAssignedUnit() + "\n" +
                "Acción recomendada: " + signal.getRecommendedAction();

        NotificationLog log = NotificationLog.builder()
                .signal(signal)
                .recipientEmail(recipient)
                .subject(subject)
                .createdAt(Instant.now())
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);

            log.setNotifStatus("SENT");
            log.setSentAt(Instant.now());
            signal.setStatus("ATENDIDA");
        } catch (Exception e) {
            log.setNotifStatus("FAILED");
            log.setErrorMessage(e.getMessage());
            signal.setStatus("ERROR");
        }

        signal.setUpdatedAt(Instant.now());
        logRepository.save(log);
        signalRepository.save(signal);
    }
}