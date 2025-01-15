package com.saisonomni.searchly_client.cdcConfigs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventPayload {
    @JsonProperty("payload")
    String payload;
    @JsonProperty("eventContext")
    EventContext eventContext;
    @JsonProperty("payloadMetadata")
    PayloadMetadata payloadMetadata;
    @JsonProperty("userContext")
    String userContext;
}
