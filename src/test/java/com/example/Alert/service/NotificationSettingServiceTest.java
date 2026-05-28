package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.dto.request.UpdateNotificationSettingRequest;
import com.example.Alert.dto.response.NotificationSettingResponse;
import com.example.Alert.entity.NotificationSetting;
import com.example.Alert.repository.NotificationSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    @Mock
    private NotificationSettingRepository settingRepository;

    private NotificationSetting existingSetting;

    @BeforeEach
    void setUp() {
        existingSetting = NotificationSetting.builder()
                .id(1L)
                .userId(42L)
                .pushEnabled(true)
                .chatEnabled(true)
                .studyEnabled(false)
                .build();
    }

    @Nested
    @DisplayName("getSetting")
    class GetSetting {

        @Test
        @DisplayName("유저 ID로 알림 설정을 반환한다")
        void success() {
            given(settingRepository.findByUserId(42L)).willReturn(Optional.of(existingSetting));

            NotificationSettingResponse result = notificationSettingService.getSetting(42L);

            assertThat(result.getUserId()).isEqualTo(42L);
            assertThat(result.isPushEnabled()).isTrue();
            assertThat(result.isChatEnabled()).isTrue();
            assertThat(result.isStudyEnabled()).isFalse();
        }

        @Test
        @DisplayName("설정이 없으면 USER_NOT_FOUND 예외를 던진다")
        void notFound() {
            given(settingRepository.findByUserId(42L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationSettingService.getSetting(42L))
                    .isInstanceOf(AppException.class)
                    .extracting("code")
                    .isEqualTo("USER_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("updateSetting")
    class UpdateSetting {

        @Test
        @DisplayName("기존 설정이 있으면 요청값으로 업데이트한다 (UPDATE)")
        void updateExisting() {
            given(settingRepository.findByUserId(42L)).willReturn(Optional.of(existingSetting));
            given(settingRepository.save(any())).willAnswer(i -> i.getArgument(0));

            UpdateNotificationSettingRequest request = new UpdateNotificationSettingRequest();
            request.setPushEnabled(false); // pushEnabled만 변경

            notificationSettingService.updateSetting(42L, request);

            // save()에 실제로 전달된 엔티티를 캡처해서 검증
            ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(settingRepository).save(captor.capture());

            assertThat(captor.getValue().getId()).isEqualTo(1L); // 기존 ID 유지 → UPDATE
            assertThat(captor.getValue().isPushEnabled()).isFalse();  // 요청값으로 변경
            assertThat(captor.getValue().isChatEnabled()).isTrue();   // 기존값 유지
            assertThat(captor.getValue().isStudyEnabled()).isFalse(); // 기존값 유지
        }

        @Test
        @DisplayName("기존 설정이 없으면 기본값으로 새로 생성한다 (INSERT)")
        void createNew() {
            given(settingRepository.findByUserId(42L)).willReturn(Optional.empty());
            given(settingRepository.save(any())).willAnswer(i -> i.getArgument(0));

            // 아무 필드도 지정하지 않은 요청
            UpdateNotificationSettingRequest request = new UpdateNotificationSettingRequest();

            notificationSettingService.updateSetting(42L, request);

            ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(settingRepository).save(captor.capture());

            assertThat(captor.getValue().getId()).isNull();           // ID 없음 → INSERT
            assertThat(captor.getValue().getUserId()).isEqualTo(42L);
            assertThat(captor.getValue().isPushEnabled()).isTrue();   // 기본값
            assertThat(captor.getValue().isChatEnabled()).isTrue();   // 기본값
            assertThat(captor.getValue().isStudyEnabled()).isFalse(); // 기본값
        }

        @Test
        @DisplayName("기존 설정이 없을 때 요청값이 있으면 해당 값으로 생성한다")
        void createNewWithValues() {
            given(settingRepository.findByUserId(42L)).willReturn(Optional.empty());
            given(settingRepository.save(any())).willAnswer(i -> i.getArgument(0));

            UpdateNotificationSettingRequest request = new UpdateNotificationSettingRequest();
            request.setPushEnabled(false);
            request.setStudyEnabled(true);

            notificationSettingService.updateSetting(42L, request);

            ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
            verify(settingRepository).save(captor.capture());

            assertThat(captor.getValue().isPushEnabled()).isFalse(); // 요청값
            assertThat(captor.getValue().isChatEnabled()).isTrue();  // 기본값
            assertThat(captor.getValue().isStudyEnabled()).isTrue(); // 요청값
        }
    }
}
