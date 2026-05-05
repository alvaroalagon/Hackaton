package com.example.hackaton_01.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TropelRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 40, message = "El nombre debe tener entre 2 y 40 caracteres")
    private String name;

    @NotBlank(message = "La especie es obligatoria")
    private String species;

    @NotNull(message = "El ID del sector es obligatorio")
    private Long sectorId;

    @NotNull(message = "El ID del guardián es obligatorio")
    private Long guardianId;
}