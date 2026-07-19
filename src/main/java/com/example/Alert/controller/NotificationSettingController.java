package com.example.Alert.controller;

import com.example.Alert.common.response.ApiResponse;
import com.example.Alert.dto.request.UpdateNotificationSettingRequest;
import com.example.Alert.dto.response.NotificationSettingResponse;
import com.example.Alert.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Setting", description = "알림 설정 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationSettingController {
    private final NotificationSettingService notificationSettingService;

    @Operation(summary = "알림 설정 조회", description = "로그인한 유저의 알림 설정(푸시, 채팅, 스터디 알림 활성화 여부)을 조회합니다.")
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings(
            @AuthenticationPrincipal Long userId) {
        NotificationSettingResponse response = notificationSettingService.getSetting(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "알림 설정을 성공적으로 가져왔습니다."));
    }

    @Operation(summary = "알림 설정 수정", description = "로그인한 유저의 알림 설정을 수정합니다. 변경할 항목만 포함해서 요청할 수 있습니다.")
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSettings(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateNotificationSettingRequest request) {
        NotificationSettingResponse response = notificationSettingService.updateSetting(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "알림 설정이 수정되었습니다."));
    }
}
