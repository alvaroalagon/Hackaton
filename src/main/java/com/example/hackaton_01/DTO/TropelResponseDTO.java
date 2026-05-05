package com.example.hackaton_01.DTO;

import lombok.Data;
import java.time.Instant;

@Data
public class TropelResponseDTO {
    private Long id;
    private String name;
    private String species;
    private String vitalState;
    private Integer energyLevel;
    private Integer chaosIndex;
    private Integer mutationStage;
    private Long sectorId;
    private Long guardianId;
    private Instant createdAt;
    private Instant updatedAt;
}