package com.example.Alert.controller;

import com.example.Alert.common.response.ApiResponse;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationListResponse;
import com.example.Alert.dto.response.NotificationReadAllResponse;
import com.example.Alert.dto.response.NotificationReadResponse;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 생성", description = "특정 유저에게 알림을 생성합니다. type은 chat / study / system 중 하나여야 합니다.")
    @PostMapping("/user")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createUserNotification(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(response, "알림이 생성되었습니다.", 201));
    }

    @Operation(summary = "알림 목록 조회", description = "로그인한 유저의 알림 목록을 조회합니다. type, is_read 필터와 페이징을 지원합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotification(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "알림 타입 필터 (chat / study / system)") @RequestParam(required = false) String type,
            @Parameter(description = "읽음 여부 필터 (true / false)") @RequestParam(name = "is_read", required = false) Boolean isRead,
            @Parameter(description = "페이지 번호 (기본값: 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수 (기본값: 20)") @RequestParam(defaultValue = "20") int limit
    ) {
        NotificationListResponse result = notificationService.getNotifications(userId, type, isRead, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "알림 목록을 성공적으로 가져왔습니다."));
    }

    @Operation(summary = "알림 단건 읽음 처리", description = "알림 ID로 특정 알림을 읽음 처리합니다. 본인 알림만 처리할 수 있습니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "읽음 처리할 알림 ID") @PathVariable Long id
    ) {
        NotificationReadResponse response = notificationService.markAsRead(userId, id);

        return ResponseEntity.ok(ApiResponse.success(response, "알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 유저의 읽지 않은 알림을 모두 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<NotificationReadAllResponse>> markAsReadAll(
            @AuthenticationPrincipal Long userId
    ) {
        NotificationReadAllResponse response = notificationService.markAsAllRead(userId);

        return ResponseEntity.ok(ApiResponse.success(response, "모든 알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "SSE 알림 구독", description = "SSE 연결을 맺어 실시간 알림을 수신합니다. 연결은 1시간 유지됩니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId) {
        return notificationService.subscribe(userId);
    }
}
