package com.example.Alert.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 요청을 보낼 때 맞춰야 하는 DTO
@Getter
@NoArgsConstructor
public class UpdateNotificationSettingRequest {

    private Boolean pushEnabled;
    private Boolean chatEnabled;
    private Boolean studyEnabled;

}
