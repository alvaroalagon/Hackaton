package com.example.hackaton_01.Service;

import com.example.hackaton_01.DTO.TropelRequestDTO;
import com.example.hackaton_01.DTO.TropelResponseDTO;
import com.example.hackaton_01.Model.Guardian;
import com.example.hackaton_01.Model.Sector;
import com.example.hackaton_01.Model.Tropel;
import com.example.hackaton_01.Repository.GuardianRepository;
import com.example.hackaton_01.Repository.SectorRepository;
import com.example.hackaton_01.Repository.TropelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TropelService {

    private final TropelRepository tropelRepository;
    private final SectorRepository sectorRepository;
    private final GuardianRepository guardianRepository;

    @Transactional
    public TropelResponseDTO createTropel(TropelRequestDTO request) {
        // 1. Validar unicidad del nombre (Regla: 409 Conflict)
        if (tropelRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un Tropel con ese nombre");
        }

        // 2. Buscar Sector (Regla: 404 Not Found)
        Sector sector = sectorRepository.findById(request.getSectorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector no encontrado"));

        // 3. Buscar Guardian (Regla: 404 Not Found)
        Guardian guardian = guardianRepository.findById(request.getGuardianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guardián no encontrado"));

        // 4. Validar capacidad del sector (Regla: 400 Bad Request)
        if (sector.getCurrentLoad() >= sector.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector está lleno");
        }

        // 5. Crear el Tropel con valores iniciales obligatorios
        Tropel tropel = Tropel.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .sector(sector)
                .guardian(guardian)
                .vitalState("ESTABLE")
                .energyLevel(80)
                .chaosIndex(10)
                .mutationStage(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 6. Incrementar carga del sector y guardarlo
        sector.setCurrentLoad(sector.getCurrentLoad() + 1);
        sectorRepository.save(sector);

        // 7. Guardar el Tropel
        Tropel savedTropel = tropelRepository.save(tropel);

        // 8. Mapear a DTO de respuesta para no devolver la entidad
        return mapToResponseDTO(savedTropel);
    }

    // Método auxiliar para mapear de Entidad a DTO
    private TropelResponseDTO mapToResponseDTO(Tropel tropel) {
        TropelResponseDTO dto = new TropelResponseDTO();
        dto.setId(tropel.getId());
        dto.setName(tropel.getName());
        dto.setSpecies(tropel.getSpecies());
        dto.setVitalState(tropel.getVitalState());
        dto.setEnergyLevel(tropel.getEnergyLevel());
        dto.setChaosIndex(tropel.getChaosIndex());
        dto.setMutationStage(tropel.getMutationStage());
        dto.setSectorId(tropel.getSector().getId());
        dto.setGuardianId(tropel.getGuardian().getId());
        dto.setCreatedAt(tropel.getCreatedAt());
        dto.setUpdatedAt(tropel.getUpdatedAt());
        return dto;
    }
}