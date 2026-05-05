package com.example.hackaton_01.Repository;

import com.example.hackaton_01.Model.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    // Necesario para el DataInitializer que buscará si el admin ya existe
    Optional<Guardian> findByEmail(String email);
}