package com.example.Alert.controller;

import com.example.Alert.common.response.ApiResponse;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // post 메서드
    @PostMapping("/user")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createUserNotification(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(response, "알림이 생성되었습니다.", 201));
    }
}
