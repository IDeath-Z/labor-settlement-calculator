package com.deathz.laborcalc.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BacenSelicDto(
    
    @JsonProperty("data") 
    String date,
    
    @JsonProperty("valor") 
    String value

) {}