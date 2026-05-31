package com.example.Alert.repository;

import com.example.Alert.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;


public interface NotificationRepository extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification> {

    // 알림을 찾을 때 id만으로 찾으면 다른 유저의 알림도 읽음 처리 되기 때문에
    // userId를 통해 함께 검증해야 한다.
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

}
