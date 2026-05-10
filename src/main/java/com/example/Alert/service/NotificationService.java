package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.entity.Notification;
import com.example.Alert.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor // final 필드를 주입받는 생성자를 자동으로 생성
public class NotificationService {
    private static final Set<String> VALID_TYPES = Set.of("chat", "study", "system");

    private final NotificationRepository notificationRepository;

    // 메서드 실행을 하나의 DB 트랜잭션으로 묶어서, 예외가 발생 했을 때 자동으로 롤백
    @Transactional
    // 새로운 알림 생성 서비스
    // 필수 필드가 비어있을 때 AppException 실행
    public NotificationResponse createUserNotification(CreateNotificationRequest request) {
        if (request.getTargetUserId() == null || request.getType() == null || request.getTitle() == null || request.getBody() == null) {
            throw new AppException(ErrorCode.MISSING_FIELDS);
        }
        // 타입 필드가 VALID_TYPES에 맞지 않으면 잘못된 타입임을 throw
        if (!VALID_TYPES.contains(request.getType())) {
            throw new AppException(ErrorCode.INVALID_TYPE);
        }

        Notification notification = Notification.builder()
                .userId(request.getTargetUserId())
                .type(request.getType())
                .title(request.getTitle())
                .body(request.getBody())
                .url(request.getUrl())
                .build();

        Notification saved = notificationRepository.save(notification);

        // 응답결과를 직접 반환하지 않고 DTO로 반환해 return
        return NotificationResponse.from(saved);
    }
}
