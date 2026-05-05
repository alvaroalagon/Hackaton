package com.example.hackaton_01.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TropelSignalRequestDTO {

    @NotNull(message = "El ID del Tropel es obligatorio")
    private Long tropelId;

    @NotNull(message = "El ID del Guardián es obligatorio")
    private Long guardianId;

    @NotBlank(message = "El senderTag es obligatorio")
    private String senderTag;

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    private String rawContent;
}