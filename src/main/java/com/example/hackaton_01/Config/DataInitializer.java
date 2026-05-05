package com.example.hackaton_01.Config;

import com.example.hackaton_01.Model.Guardian;
import com.example.hackaton_01.Repository.GuardianRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GuardianRepository guardianRepository;

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.notification.email}")
    private String adminNotificationEmail;

    public DataInitializer(GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (guardianRepository.findByEmail(adminEmail).isEmpty()) {

            Guardian admin = Guardian.builder()
                    .displayName(adminName)
                    .email(adminEmail)
                    .notificationEmail(adminNotificationEmail)
                    .createdAt(Instant.now())
                    .build();

            guardianRepository.save(admin);
            System.out.println("✅ Primer guardián (" + adminName + ") creado exitosamente.");

        } else {
            System.out.println("⚡ El guardián administrador ya existe en la base de datos. Omitiendo creación.");
        }
    }
}