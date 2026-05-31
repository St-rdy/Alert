package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationListResponse;
import com.example.Alert.dto.response.NotificationReadAllResponse;
import com.example.Alert.dto.response.NotificationReadResponse;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.entity.Notification;
import com.example.Alert.repository.NotificationRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Nested
    @DisplayName("getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("필터 없이 조회하면 전체 알림 목록을 반환한다")
        void success() {
            Page<Notification> mockPage = new PageImpl<>(List.of(mockNotification));
            given(notificationRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(mockPage);
            given(notificationRepository.count(any(Predicate.class))).willReturn(1L);

            NotificationListResponse result = notificationService.getNotifications(42L, null, null, 1, 20);

            assertThat(result.getUnreadCount()).isEqualTo(1L);
            assertThat(result.getPagination().getPage()).isEqualTo(1);
            assertThat(result.getPagination().getTotal()).isEqualTo(1L);
            assertThat(result.getNotifications()).hasSize(1);
            assertThat(result.getNotifications().get(0).getType()).isEqualTo("chat");
        }

        @Test
        @DisplayName("알림이 없으면 빈 목록을 반환한다")
        void empty() {
            Page<Notification> emptyPage = new PageImpl<>(List.of());
            given(notificationRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(emptyPage);
            given(notificationRepository.count(any(Predicate.class))).willReturn(0L);

            NotificationListResponse result = notificationService.getNotifications(42L, null, null, 1, 20);

            assertThat(result.getNotifications()).isEmpty();
            assertThat(result.getUnreadCount()).isZero();
            assertThat(result.getPagination().getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("markAsAllRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("읽지 않은 알림이 있으면 업데이트된 수를 반환한다")
        void success() {
            given(notificationRepository.markAllAsReadByUserId(42L)).willReturn(3);

            NotificationReadAllResponse result = notificationService.markAsAllRead(42L);

            assertThat(result.getUpdatedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("읽지 않은 알림이 없으면 0을 반환한다")
        void noUnread() {
            given(notificationRepository.markAllAsReadByUserId(42L)).willReturn(0);

            NotificationReadAllResponse result = notificationService.markAsAllRead(42L);

            assertThat(result.getUpdatedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("알림을 읽음 처리하면 is_read가 true가 된다")
        void success() {
            given(notificationRepository.findByIdAndUserId(1L, 42L))
                    .willReturn(Optional.of(mockNotification));
            given(notificationRepository.save(any())).willReturn(mockNotification);

            NotificationReadResponse result = notificationService.markAsRead(42L, 1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.isRead()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 알림이면 NOTIFICATION_NOT_FOUND 예외를 던진다")
        void notFound() {
            given(notificationRepository.findByIdAndUserId(999L, 42L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(42L, 999L))
                    .isInstanceOf(AppException.class)
                    .extracting("code")
                    .isEqualTo("NOT_FOUND");
        }
    }
}
