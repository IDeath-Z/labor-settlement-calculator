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

import com.deathz.laborcalc.domain.model.SelicRate;
import com.deathz.laborcalc.domain.ports.BacenGateway;
import com.deathz.laborcalc.infrastructure.client.dto.BacenSelicDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BacenApiClient implements BacenGateway{

    private final String selicBaseUrl;
    private final String selicFilterUrl;
    private final HttpClient httpClient;
    private final DateTimeFormatter formatter;
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private final ObjectMapper objectMapper;

    public BacenApiClient(@Value("${bacen.api.sgs.selic.url}") String selicBaseUrl, @Value("${bacen.api.sgs.selic.url.filter}") String selicFilterUrl) {
        this.selicBaseUrl = selicBaseUrl;
        this.selicFilterUrl = selicFilterUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<SelicRate> getSelicRateHistory(LocalDate startDate, LocalDate endDate) {
        
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        String filterQuery = String.format(this.selicFilterUrl, startDateStr, endDateStr);

        String finalUrl = this.selicBaseUrl + filterQuery;

        return fetchSelicRates(finalUrl);
    }

    private List<SelicRate> fetchSelicRates(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                List<BacenSelicDto> selicDtos = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<List<BacenSelicDto>>() {}
                );

                return selicMapper(selicDtos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Selic rate history", e);
        }
    }

    private List<SelicRate> selicMapper(List<BacenSelicDto> selicDtos) {
        return selicDtos.stream()
            .map(dto -> new SelicRate(
                LocalDate.parse(dto.date(), formatter), 
                new BigDecimal(dto.value())))
            .toList();
    }
}
