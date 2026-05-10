package com.example.Alert.controller;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.response.NotificationResponse;
import com.example.Alert.security.SecurityConfig;
import com.example.Alert.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// @TestPropertySource: SecurityConfig의 @Value("${jwt.secret}") 주입을 위해 필요
@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.secret=test-secret-key-must-be-32-chars!!")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse mockResponse;

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
                            .with(jwt().jwt(b -> b.claim("id", 1)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.targetUserId").value(42))
                    .andExpect(jsonPath("$.data.read").value(false));
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

            // .with(jwt()) 없이 요청 — SecurityConfig의 authenticationEntryPoint가 401 반환
            mockMvc.perform(post("/api/v1/notifications/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("필수 필드가 없으면 MISSING_FIELDS를 반환한다 (400)")
        void missingFields() throws Exception {
            // 서비스가 MISSING_FIELDS 예외를 던지면 GlobalExceptionHandler가 400으로 처리
            given(notificationService.createUserNotification(any()))
                    .willThrow(new AppException(ErrorCode.MISSING_FIELDS));

            Map<String, Object> body = Map.of( // targetUserId 누락
                    "type", "chat",
                    "title", "새 메시지가 도착했어요",
                    "body", "허태범님이 메시지를 보냈습니다."
            );

            mockMvc.perform(post("/api/v1/notifications/user")
                            .with(jwt().jwt(b -> b.claim("id", 1)))
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
                            .with(jwt().jwt(b -> b.claim("id", 1)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_TYPE"));
        }
    }
}
