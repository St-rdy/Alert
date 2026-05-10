package com.example.Alert.controller;

import com.example.Alert.common.response.ApiResponse;
import com.example.Alert.dto.request.UpdateNotificationSettingRequest;
import com.example.Alert.dto.response.NotificationSettingResponse;
import com.example.Alert.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationSettingController {
    private final NotificationSettingService notificationSettingService;

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        NotificationSettingResponse response = notificationSettingService.getSetting(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "알림 설정을 성공적으로 가져왔습니다."));
    }

    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSettings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateNotificationSettingRequest request) {
        Long userId = extractUserId(jwt);
        NotificationSettingResponse response = notificationSettingService.updateSetting(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "알림 설정이 수정되었습니다."));
    }

    private Long extractUserId(Jwt jwt) {
        return ((Number) jwt.getClaim("id")).longValue();
    }
}
