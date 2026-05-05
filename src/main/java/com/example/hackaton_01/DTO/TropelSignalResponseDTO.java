package com.example.hackaton_01.DTO;

import lombok.Data;
import java.time.Instant;

@Data
public class TropelSignalResponseDTO {
    private Long id;
    private String signalType;
    private String severity;
    private String status;
    private String recommendedAction;
    private Instant createdAt;
}