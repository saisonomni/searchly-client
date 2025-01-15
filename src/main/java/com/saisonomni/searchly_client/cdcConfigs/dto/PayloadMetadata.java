package com.saisonomni.searchly_client.cdcConfigs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayloadMetadata {
    @JsonProperty("destination")
    private String destination;
    @JsonProperty("destinationType")
    private String destinationType;

}
