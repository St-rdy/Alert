package com.example.Alert.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadAllResponse {

    @JsonProperty("updated_count")
    private int updatedCount;
}
