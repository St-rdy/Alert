package com.example.Alert.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// 요청을 보낼 때 맞춰야 하는 DTO
@Getter
@Setter
@NoArgsConstructor
public class CreateNotificationRequest {
    private Long targetUserId;
    private String type;
    private String title;
    private String body;
    private String url;
}
