package com.example.Alert.service;

import com.example.Alert.common.exception.AppException;
import com.example.Alert.common.exception.ErrorCode;
import com.example.Alert.dto.request.UpdateNotificationSettingRequest;
import com.example.Alert.dto.response.NotificationSettingResponse;
import com.example.Alert.entity.NotificationSetting;
import com.example.Alert.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final NotificationSettingRepository settingRepository;

    // 읽기 전용
    @Transactional(readOnly = true)
    // 유저 정보를 입력해, 유저의 설정 값을 불러오는 서비스
    public NotificationSettingResponse getSetting(Long userId) {
        // 알림 설졍을 유저 ID를 통해 검색하는데, 잘못된 userId를 입력할 경우 USER_NOT_FOUND를 리턴
        NotificationSetting setting = settingRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // 리턴값을 DTO로 변환하여 반환
        return NotificationSettingResponse.from(setting);
    }

    @Transactional
    public NotificationSettingResponse updateSetting(Long userId, UpdateNotificationSettingRequest request) {
        // 유저의 기존 정보를 조회
        NotificationSetting existing = settingRepository.findByUserId(userId).orElse(null);

        NotificationSetting toSave;

        // 기존 유저의 정보가 비어 있다면, 기본 설정
        if (existing == null) {
            toSave = NotificationSetting.builder()
                    .userId(userId)
                    .pushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : true)
                    .chatEnabled(request.getChatEnabled() != null ? request.getChatEnabled() : true)
                    .studyEnabled(request.getStudyEnabled() != null ? request.getStudyEnabled() : false)
                    .build();
        } else {
            // 설정값이 있다면, 입력 받은 값으로 치환 UPDATE 대응
            toSave = NotificationSetting.builder()
                    .id(existing.getId())
                    .userId(existing.getUserId())
                    .pushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : existing.isPushEnabled())
                    .chatEnabled(request.getChatEnabled() != null ? request.getChatEnabled() : existing.isChatEnabled())
                    .studyEnabled(request.getStudyEnabled() != null ? request.getStudyEnabled() : existing.isStudyEnabled())
                    .build();
        }

        // DTO로 반환
        return NotificationSettingResponse.from(settingRepository.save(toSave));
    }
}
