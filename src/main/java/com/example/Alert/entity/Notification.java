package com.example.Alert.entity;

// JPA
import jakarta.persistence.*;
// lombok, getter, setter, builder 자동 생성
import lombok.*;
// Insert 시점에 자동으로 현재 시간을 넣어줌
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "notification")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    // 아이디
    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 알림 타입
    @Column(nullable = false, length = 50)
    private String type;

    // 타이틀
    @Column(nullable = false, length = 255)
    private String title;

    // 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    // 읽었는가
    @Column(name = "is_read", nullable = false)
    private boolean read;

    // 이동 url
    @Column
    private String url;

    // 생성 시간
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
