package com.deathz.laborcalc.infrastructure.client;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.deathz.laborcalc.application.exceptions.ExternalServiceNoDataFoundException;
import com.deathz.laborcalc.application.exceptions.enums.ExternalServiceNoDataFoundErrorMessage;
import com.deathz.laborcalc.domain.model.MinimumWage;
import com.deathz.laborcalc.domain.ports.MinimumWageGateway;
import com.deathz.laborcalc.infrastructure.client.dto.BacenSgsDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BacenMinimumWageApiClient implements MinimumWageGateway {

    private final String minimumWageBaseUrl;
    private final String minimumWageFilterUrl;
    private final HttpClient httpClient;
    private final DateTimeFormatter formatter;
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private final ObjectMapper objectMapper;

    public BacenMinimumWageApiClient(@Value("${bacen.api.sgs.wage.url}") String minimumWageBaseUrl, @Value("${bacen.api.sgs.url.filter}") String minimumWageFilterUrl) {
        this.minimumWageBaseUrl = minimumWageBaseUrl;
        this.minimumWageFilterUrl = minimumWageFilterUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<MinimumWage> getMinimumWageHistory(LocalDate startDate, LocalDate endDate) {
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        String filterQuery = String.format(this.minimumWageFilterUrl, startDateStr, endDateStr);

        String finalUrl = this.minimumWageBaseUrl + filterQuery;

        return fetchMinimumWageRates(finalUrl);
    }

    private List<MinimumWage> fetchMinimumWageRates(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                List<BacenSgsDto> minimumWageDtos = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<BacenSgsDto>>() {}
                );

                return minimumWageMapper(minimumWageDtos);
        } catch (Exception e) {
            throw new ExternalServiceNoDataFoundException(ExternalServiceNoDataFoundErrorMessage.BACEN_MINIMUM_WAGE_CONNECTION_ERROR.getMessage(), e);
        }
    }

    private List<MinimumWage> minimumWageMapper(List<BacenSgsDto> minimumWageDtos) {
        return minimumWageDtos.stream()
            .map(dto -> new MinimumWage(
                LocalDate.parse(dto.date(), formatter), 
                new BigDecimal(dto.value())))
            .toList();
    }
}
