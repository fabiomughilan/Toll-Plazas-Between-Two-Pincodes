package com.freightfox.tollplaza.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freightfox.tollplaza.dto.TollPlazaRequest;
import com.freightfox.tollplaza.dto.TollPlazaResponse;
import com.freightfox.tollplaza.service.TollPlazaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/toll-plazas")
@Tag(name = "Toll Plazas API", description = "Endpoints for discovering toll plazas between Indian pincodes")
public class TollPlazaController {

    private final TollPlazaService tollPlazaService;

    public TollPlazaController(TollPlazaService tollPlazaService) {
        this.tollPlazaService = tollPlazaService;
    }

    @PostMapping
    @Operation(summary = "Get Toll Plazas between two pincodes", description = "Calculates driving route between source and destination pincodes and returns all along-route toll plazas.")
    public ResponseEntity<TollPlazaResponse> getTollPlazas(@Valid @RequestBody TollPlazaRequest request) {
        TollPlazaResponse response = tollPlazaService.getTollPlazasBetweenPincodes(request);
        return ResponseEntity.ok(response);
    }
}
