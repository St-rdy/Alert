package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.request.CreateNotificationRequest;
import com.example.Alert.dto.response.NotificationListResponse;
import com.example.Alert.dto.response.NotificationReadResponse;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.dto.response.PaginationResponse;
import com.example.Alert.entity.Notification;
import com.example.Alert.entity.QNotification;
import com.example.Alert.repository.NotificationRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public NotificationListResponse getNotifications(Long userId, String type, Boolean isRead, int page, int limit) {

        // 동적 조건
        // BooleanBuilder를 통해 and로 조건을 하나씩 붙임
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(QNotification.notification.userId.eq(userId));
        if (type != null) predicate.and(QNotification.notification.type.eq(type));
        if (isRead != null) predicate.and(QNotification.notification.read.eq(isRead));
        // 페이징 설정
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        // 조건 + 페이징으로 DB를 조회
        Page<Notification> result = notificationRepository.findAll(predicate, pageable);

        // 안읽은 알림 수 카운트
        long unreadCount = notificationRepository.count(
                QNotification.notification.userId.eq(userId).and(QNotification.notification.read.eq(false))
        );

        return NotificationListResponse.builder()
                .unreadCount(unreadCount)
                .pagination(PaginationResponse.builder()
                        .page(page)
                        .limit(limit)
                        .total(result.getTotalElements())
                        .build())
                .notifications(result.getContent().stream()
                        .map(NotificationResponse::from)
                        .toList())
                .build();

    }

    // 알림 읽음 처리
    public NotificationReadResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();

        return NotificationReadResponse.from(notificationRepository.save(notification));
    }

}
