package com.example.Alert.controller;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.response.NotificationListResponse;
import com.example.Alert.dto.response.NotificationReadResponse;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.dto.response.PaginationResponse;
import com.example.Alert.security.JwtAuthenticationFilter;
import com.example.Alert.security.JwtProvider;
import com.example.Alert.security.SecurityConfig;
import com.example.Alert.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// JwtAuthenticationFilter를 @Import로 직접 포함 — 실제 필터가 동작해야 401이 정상 동작함
// JwtProvider만 mock — 실제 jwt.secret 없이도 필터가 생성되고, 헤더 없는 요청은 인증 없이 통과
@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    // JwtProvider만 mock: jwt.secret 주입 없이도 컨텍스트 로드 가능
    // 실제 필터(JwtAuthenticationFilter)는 그대로 동작하므로 필터 체인이 정상 실행됨
    @MockitoBean
    private JwtProvider jwtProvider;

    private NotificationResponse mockResponse;

    // principal 자리에 userId(42L)를 넣어 @AuthenticationPrincipal Long userId 와 연결
    private final UsernamePasswordAuthenticationToken mockAuth =
            new UsernamePasswordAuthenticationToken(42L, null, Collections.emptyList());

    @BeforeEach
    void setUp() {
        mockResponse = NotificationResponse.builder()
                .id(1L)
                .targetUserId(42L)
                .type("chat")
                .title("새 메시지가 도착했어요")
                .body("허태범님이 메시지를 보냈습니다.")
                .read(false)
                .url("/chat/room/5")
                .createdAt(LocalDateTime.of(2025, 2, 25, 10, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/user")
    class CreateNotification {

        @Test
        @DisplayName("유효한 요청이면 알림을 생성한다 (201)")
        void success() throws Exception {
            given(notificationService.createUserNotification(any())).willReturn(mockResponse);

            Map<String, Object> body = Map.of(
                    "targetUserId", 42,
                    "type", "chat",
                    "title", "새 메시지가 도착했어요",
                    "body", "허태범님이 메시지를 보냈습니다.",
                    "url", "/chat/room/5"
            );

            mockMvc.perform(post("/api/v1/notifications/user")
                            .with(authentication(mockAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.targetUserId").value(42))
                    .andExpect(jsonPath("$.data.is_read").value(false));
        }

        @Test
        @DisplayName("토큰이 없으면 UNAUTHORIZED를 반환한다 (401)")
        void noToken() throws Exception {
            Map<String, Object> body = Map.of(
                    "targetUserId", 42,
                    "type", "chat",
                    "title", "새 메시지가 도착했어요",
                    "body", "허태범님이 메시지를 보냈습니다."
            );

            // authentication() 없이 요청 — SecurityContext 비어 있음 → 401
            mockMvc.perform(post("/api/v1/notifications/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("필수 필드가 없으면 MISSING_FIELDS를 반환한다 (400)")
        void missingFields() throws Exception {
            given(notificationService.createUserNotification(any()))
                    .willThrow(new AppException(ErrorCode.MISSING_FIELDS));

            Map<String, Object> body = Map.of(
                    "type", "chat",
                    "title", "새 메시지가 도착했어요",
                    "body", "허태범님이 메시지를 보냈습니다."
            );

            mockMvc.perform(post("/api/v1/notifications/user")
                            .with(authentication(mockAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MISSING_FIELDS"));
        }

        @Test
        @DisplayName("유효하지 않은 type이면 INVALID_TYPE을 반환한다 (400)")
        void invalidType() throws Exception {
            given(notificationService.createUserNotification(any()))
                    .willThrow(new AppException(ErrorCode.INVALID_TYPE));

            Map<String, Object> body = Map.of(
                    "targetUserId", 42,
                    "type", "invalid_type",
                    "title", "새 메시지가 도착했어요",
                    "body", "허태범님이 메시지를 보냈습니다."
            );

            mockMvc.perform(post("/api/v1/notifications/user")
                            .with(authentication(mockAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_TYPE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록을 반환한다 (200)")
        void success() throws Exception {
            NotificationListResponse mockList = NotificationListResponse.builder()
                    .unreadCount(1L)
                    .pagination(PaginationResponse.builder().page(1).limit(20).total(1L).build())
                    .notifications(List.of(mockResponse))
                    .build();

            given(notificationService.getNotifications(42L, null, null, 1, 20)).willReturn(mockList);

            mockMvc.perform(get("/api/v1/notifications")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.unread_count").value(1))
                    .andExpect(jsonPath("$.data.pagination.page").value(1))
                    .andExpect(jsonPath("$.data.pagination.total").value(1))
                    .andExpect(jsonPath("$.data.notifications[0].type").value("chat"))
                    .andExpect(jsonPath("$.data.notifications[0].is_read").value(false));
        }

        @Test
        @DisplayName("type 필터로 조회한다 (200)")
        void filterByType() throws Exception {
            NotificationListResponse mockList = NotificationListResponse.builder()
                    .unreadCount(1L)
                    .pagination(PaginationResponse.builder().page(1).limit(20).total(1L).build())
                    .notifications(List.of(mockResponse))
                    .build();

            given(notificationService.getNotifications(42L, "chat", null, 1, 20)).willReturn(mockList);

            mockMvc.perform(get("/api/v1/notifications")
                            .param("type", "chat")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notifications[0].type").value("chat"));
        }

        @Test
        @DisplayName("토큰이 없으면 UNAUTHORIZED를 반환한다 (401)")
        void noToken() throws Exception {
            mockMvc.perform(get("/api/v1/notifications"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("알림을 읽음 처리한다 (200)")
        void success() throws Exception {
            NotificationReadResponse mockReadResponse = NotificationReadResponse.builder()
                    .id(1L)
                    .read(true)
                    .build();

            given(notificationService.markAsRead(42L, 1L)).willReturn(mockReadResponse);

            mockMvc.perform(patch("/api/v1/notifications/1/read")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.is_read").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 알림이면 NOT_FOUND를 반환한다 (404)")
        void notFound() throws Exception {
            given(notificationService.markAsRead(42L, 999L))
                    .willThrow(new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

            mockMvc.perform(patch("/api/v1/notifications/999/read")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("토큰이 없으면 UNAUTHORIZED를 반환한다 (401)")
        void noToken() throws Exception {
            mockMvc.perform(patch("/api/v1/notifications/1/read"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
    }
}
