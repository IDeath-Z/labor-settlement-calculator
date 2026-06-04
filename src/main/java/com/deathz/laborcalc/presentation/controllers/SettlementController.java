package com.deathz.laborcalc.presentation.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deathz.laborcalc.application.usecases.CalculateLaborSettlementUseCase;
import com.deathz.laborcalc.domain.model.SettlementResult;
import com.deathz.laborcalc.presentation.dto.SettlementInputRequest;
import com.deathz.laborcalc.presentation.mapper.SettlementInputMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlement")
public class SettlementController {

    private final CalculateLaborSettlementUseCase calculateLaborSettlementUseCase;
    private final SettlementInputMapper settlementInputMapper;

    @PostMapping("/json")
    public ResponseEntity<List<SettlementResult>> getMethodName(@RequestBody @Valid SettlementInputRequest request) {
        var input = settlementInputMapper.toDomain(request);
        return ResponseEntity.ok(calculateLaborSettlementUseCase.execute(input));
    }
    
}
