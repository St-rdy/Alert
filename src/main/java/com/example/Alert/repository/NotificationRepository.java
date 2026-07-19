package com.example.Alert.repository;

import com.example.Alert.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface NotificationRepository extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification> {

    // 알림을 찾을 때 id만으로 찾으면 다른 유저의 알림도 읽음 처리 되기 때문에
    // userId를 통해 함께 검증해야 한다.
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    // 하나씩 꺼내서 업데이트하면 오래걸리기 때문에, 유저 ID를 통해 안읽음 알림을 전체 조회한 뒤
    // 읽음 처리 쿼리 작성
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

}
