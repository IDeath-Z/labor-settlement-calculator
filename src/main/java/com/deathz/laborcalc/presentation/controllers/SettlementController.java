package com.deathz.laborcalc.presentation.controllers;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deathz.laborcalc.application.usecases.CalculateLaborSettlementUseCase;
import com.deathz.laborcalc.application.usecases.GenerateReportUseCase;
import com.deathz.laborcalc.domain.enums.ReportFormat;
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
    private final GenerateReportUseCase generateReportUseCase;
    private final SettlementInputMapper settlementInputMapper;

    private static final String SPREADSHEET_HEADER = "attachment; filename=settlement_report.xlsx";
    private static final String SPREADSHEET_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @PostMapping("/json")
    public ResponseEntity<List<SettlementResult>> getSettlementJson(@RequestBody @Valid SettlementInputRequest request) {
        var input = settlementInputMapper.toDomain(request);
        return ResponseEntity.ok(calculateLaborSettlementUseCase.execute(input));
    }

    @PostMapping("/spreadsheet")
    public ResponseEntity<byte[]> generateSpreadsheetReport(@RequestBody @Valid SettlementInputRequest request) {
        var input = settlementInputMapper.toDomain(request);
        byte[] report = generateReportUseCase.execute(calculateLaborSettlementUseCase.execute(input), ReportFormat.SPREADSHEET);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, SPREADSHEET_HEADER)
            .contentType(MediaType.parseMediaType(SPREADSHEET_MEDIA_TYPE))
            .body(report);
    }
}
