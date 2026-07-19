package com.example.Alert.dto.response;

import com.example.Alert.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadResponse {
    private Long id;

    @JsonProperty("is_read")
    private boolean read;

    public static NotificationReadResponse from(Notification notification) {
        return NotificationReadResponse.builder()
                .id(notification.getId())
                .read(notification.isRead())
                .build();
    }
}
