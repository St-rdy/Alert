package com.example.Alert.dto.response;

import com.example.Alert.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private Long targetUserId;
    private String type;
    private String title;
    private String body;
    @JsonProperty("is_read")
    private boolean read;
    private String url;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // 응답 결과에 대한 DTO
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .targetUserId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .read(notification.isRead())
                .url(notification.getUrl())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
