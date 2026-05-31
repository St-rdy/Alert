package com.example.Alert.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    @JsonProperty("unread_count")
    private long unreadCount;
    private PaginationResponse pagination;
    private List<NotificationResponse> notifications;
}
