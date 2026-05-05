package com.example.hackaton_01.Controller;

import com.example.hackaton_01.DTO.TropelSignalRequestDTO;
import com.example.hackaton_01.DTO.TropelSignalResponseDTO;
import com.example.hackaton_01.Service.TropelSignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
public class TropelSignalController {

    private final TropelSignalService signalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Obligatorio
    public TropelSignalResponseDTO processSignal(@Valid @RequestBody TropelSignalRequestDTO request) {
        return signalService.processSignal(request);
    }
}