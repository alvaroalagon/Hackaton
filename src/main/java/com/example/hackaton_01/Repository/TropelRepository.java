package com.example.hackaton_01.Repository;

import com.example.hackaton_01.Model.Tropel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TropelRepository extends JpaRepository<Tropel, Long> {
    boolean existsByName(String name);
}