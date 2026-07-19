package com.example.Alert.controller;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.response.NotificationSettingResponse;
import com.example.Alert.security.JwtAuthenticationFilter;
import com.example.Alert.security.JwtProvider;
import com.example.Alert.security.SecurityConfig;
import com.example.Alert.service.NotificationSettingService;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationSettingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class NotificationSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

    // JwtProvider만 mock: jwt.secret 주입 없이도 컨텍스트 로드 가능
    // 실제 필터(JwtAuthenticationFilter)는 그대로 동작하므로 필터 체인이 정상 실행됨
    @MockitoBean
    private JwtProvider jwtProvider;

    private NotificationSettingResponse mockResponse;

    // principal 자리에 userId(42L)를 넣어 @AuthenticationPrincipal Long userId 와 연결
    private final UsernamePasswordAuthenticationToken mockAuth =
            new UsernamePasswordAuthenticationToken(42L, null, Collections.emptyList());

    @BeforeEach
    void setUp() {
        mockResponse = NotificationSettingResponse.builder()
                .id(1L)
                .userId(42L)
                .pushEnabled(true)
                .chatEnabled(true)
                .studyEnabled(false)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/settings")
    class GetSetting {

        @Test
        @DisplayName("유효한 토큰이면 알림 설정을 반환한다 (200)")
        void success() throws Exception {
            given(notificationSettingService.getSetting(42L)).willReturn(mockResponse);

            mockMvc.perform(get("/api/v1/notifications/settings")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.userId").value(42))
                    .andExpect(jsonPath("$.data.pushEnabled").value(true))
                    .andExpect(jsonPath("$.data.chatEnabled").value(true))
                    .andExpect(jsonPath("$.data.studyEnabled").value(false));
        }

        @Test
        @DisplayName("토큰이 없으면 UNAUTHORIZED를 반환한다 (401)")
        void noToken() throws Exception {
            mockMvc.perform(get("/api/v1/notifications/settings"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("설정이 없는 유저면 USER_NOT_FOUND를 반환한다 (404)")
        void userNotFound() throws Exception {
            given(notificationSettingService.getSetting(42L))
                    .willThrow(new AppException(ErrorCode.USER_NOT_FOUND));

            mockMvc.perform(get("/api/v1/notifications/settings")
                            .with(authentication(mockAuth)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/notifications/settings")
    class UpdateSetting {

        @Test
        @DisplayName("유효한 토큰이면 알림 설정을 수정한다 (200)")
        void success() throws Exception {
            NotificationSettingResponse updatedResponse = NotificationSettingResponse.builder()
                    .id(1L)
                    .userId(42L)
                    .pushEnabled(false)
                    .chatEnabled(false)
                    .studyEnabled(false)
                    .build();

            given(notificationSettingService.updateSetting(eq(42L), any())).willReturn(updatedResponse);

            Map<String, Object> body = Map.of(
                    "pushEnabled", false,
                    "chatEnabled", false,
                    "studyEnabled", false
            );

            mockMvc.perform(patch("/api/v1/notifications/settings")
                            .with(authentication(mockAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.pushEnabled").value(false))
                    .andExpect(jsonPath("$.data.chatEnabled").value(false))
                    .andExpect(jsonPath("$.data.studyEnabled").value(false));
        }

        @Test
        @DisplayName("토큰이 없으면 UNAUTHORIZED를 반환한다 (401)")
        void noToken() throws Exception {
            Map<String, Object> body = Map.of("pushEnabled", false);

            mockMvc.perform(patch("/api/v1/notifications/settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("일부 필드만 보내도 수정된다 (PATCH 부분 업데이트)")
        void partialUpdate() throws Exception {
            NotificationSettingResponse partialResponse = NotificationSettingResponse.builder()
                    .id(1L)
                    .userId(42L)
                    .pushEnabled(false)
                    .chatEnabled(true)
                    .studyEnabled(false)
                    .build();

            given(notificationSettingService.updateSetting(eq(42L), any())).willReturn(partialResponse);

            Map<String, Object> body = Map.of("pushEnabled", false);

            mockMvc.perform(patch("/api/v1/notifications/settings")
                            .with(authentication(mockAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pushEnabled").value(false))
                    .andExpect(jsonPath("$.data.chatEnabled").value(true));
        }
    }
}
