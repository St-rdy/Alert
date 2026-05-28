package com.example.Alert.dto.response;

import com.example.Alert.entity.NotificationSetting;
import lombok.Builder;
import lombok.Getter;


// 알림 설정에 대한 응답 결과값에 대한 DTO
@Getter
@Builder
public class NotificationSettingResponse {
    private Long id;
    private Long userId;
    private boolean pushEnabled;
    private boolean chatEnabled;
    private boolean studyEnabled;

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .id(setting.getId())
                .userId(setting.getUserId())
                .pushEnabled(setting.isPushEnabled())
                .chatEnabled(setting.isChatEnabled())
                .studyEnabled(setting.isStudyEnabled())
                .build();

    }
}
