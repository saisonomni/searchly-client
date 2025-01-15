package com.saisonomni.searchly_client.cdcConfigs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventContext {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("source")
    private String source;
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("createdAt")
    private long createdAt;
    @JsonProperty("delay")
    private long delay;
}
