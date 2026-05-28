package com.example.Alert.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private long unreadCount;
    private PaginationResponse pagination;
    private List<NotificationResponse> notifications;
}
