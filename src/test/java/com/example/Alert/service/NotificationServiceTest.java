package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.entity.Notification;
import com.example.Alert.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

// Spring 컨텍스트 없이 Mockito만으로 순수 단위 테스트
// Node의 jest.mock('../../repositories/notification.repository')에 해당
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = Notification.builder()
                .id(1L)
                .userId(42L)
                .type("chat")
                .title("새 메시지가 도착했어요")
                .body("허태범님이 메시지를 보냈습니다.")
                .url("/chat/room/5")
                .createdAt(LocalDateTime.of(2025, 2, 25, 10, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("createUserNotification")
    class CreateUserNotification {

        @Test
        @DisplayName("유효한 요청이면 알림을 생성한다")
        void success() {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTargetUserId(42L);
            request.setType("chat");
            request.setTitle("새 메시지가 도착했어요");
            request.setBody("허태범님이 메시지를 보냈습니다.");
            request.setUrl("/chat/room/5");

            given(notificationRepository.save(any())).willReturn(mockNotification);

            NotificationResponse result = notificationService.createUserNotification(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTargetUserId()).isEqualTo(42L);
            assertThat(result.getType()).isEqualTo("chat");
            assertThat(result.isRead()).isFalse();
        }

        @Test
        @DisplayName("targetUserId가 없으면 MISSING_FIELDS 예외를 던진다")
        void missingTargetUserId() {
            CreateNotificationRequest request = new CreateNotificationRequest();
            // targetUserId 누락
            request.setType("chat");
            request.setTitle("새 메시지가 도착했어요");
            request.setBody("허태범님이 메시지를 보냈습니다.");

            assertThatThrownBy(() -> notificationService.createUserNotification(request))
                    .isInstanceOf(AppException.class)
                    .extracting("code")
                    .isEqualTo("MISSING_FIELDS");
        }

        @Test
        @DisplayName("body가 없으면 MISSING_FIELDS 예외를 던진다")
        void missingBody() {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTargetUserId(42L);
            request.setType("chat");
            request.setTitle("새 메시지가 도착했어요");
            // body 누락

            assertThatThrownBy(() -> notificationService.createUserNotification(request))
                    .isInstanceOf(AppException.class)
                    .extracting("code")
                    .isEqualTo("MISSING_FIELDS");
        }

        @Test
        @DisplayName("유효하지 않은 type이면 INVALID_TYPE 예외를 던진다")
        void invalidType() {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTargetUserId(42L);
            request.setType("invalid_type");
            request.setTitle("새 메시지가 도착했어요");
            request.setBody("허태범님이 메시지를 보냈습니다.");

            assertThatThrownBy(() -> notificationService.createUserNotification(request))
                    .isInstanceOf(AppException.class)
                    .extracting("code")
                    .isEqualTo("INVALID_TYPE");
        }
    }
}
