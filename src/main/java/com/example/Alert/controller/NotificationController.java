package com.example.Alert.controller;

import com.example.Alert.common.response.ApiResponse;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationListResponse;
import com.example.Alert.dto.response.NotificationReadResponse;
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

    // GET /api/v1/notification 요청 처리
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotification(
            @AuthenticationPrincipal Long userId, // userId
            @RequestParam(required = false) String type, // 파라미터 없으면, null
            @RequestParam(name = "is_read", required = false) Boolean isRead, // is_read 매핑
            @RequestParam(defaultValue = "1") int page, // 없으면 기본
            @RequestParam(defaultValue = "20") int limit // 없으면 기본
    ) {
        NotificationListResponse result = notificationService.getNotifications(userId, type, isRead, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "알림 목록을 성공적으로 가져왔습니다."));
    }

    // 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        NotificationReadResponse response = notificationService.markAsRead(userId, id);

        return ResponseEntity.ok(ApiResponse.success(response, "알림을 읽음 처리했습니다."));
    }
}
