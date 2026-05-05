package com.example.hackaton_01.Repository;

import com.example.hackaton_01.Model.CareResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareResponseRepository extends JpaRepository<CareResponse, Long> {
}