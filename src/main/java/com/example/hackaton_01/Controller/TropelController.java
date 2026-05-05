package com.example.hackaton_01.Controller;

import com.example.hackaton_01.DTO.TropelRequestDTO;
import com.example.hackaton_01.DTO.TropelResponseDTO;
import com.example.hackaton_01.Service.TropelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tropeles")
@RequiredArgsConstructor
public class TropelController {

    private final TropelService tropelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve 201
    public TropelResponseDTO createTropel(@Valid @RequestBody TropelRequestDTO request) {
        return tropelService.createTropel(request);
    }
}