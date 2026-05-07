package com.example.Alert.entity;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "notification_setting")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Builder.Default
    @Column(name = "chat_enabled", nullable = false)
    private boolean chatEnabled = true;

    @Builder.Default
    @Column(name = "study_enabled", nullable = false)
    private boolean studyEnabled = false;

}
